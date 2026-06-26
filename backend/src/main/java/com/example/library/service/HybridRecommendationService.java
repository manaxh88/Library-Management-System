package com.example.library.service;

import com.example.library.entity.BookInfo;
import com.example.library.entity.BorrowRecord;
import com.example.library.entity.OperationLog;
import com.example.library.entity.RecommendationConfig;
import com.example.library.entity.RecommendationList;
import com.example.library.entity.SysUser;
import com.example.library.repository.BookInfoRepository;
import com.example.library.repository.BorrowRecordRepository;
import com.example.library.repository.OperationLogRepository;
import com.example.library.repository.RecommendationConfigRepository;
import com.example.library.repository.RecommendationListRepository;
import com.example.library.repository.SysUserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 混合推荐服务（Java + Python）
 */
@Service
public class HybridRecommendationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HybridRecommendationService.class);
    private static final int ROLE_STUDENT = 2;
    private static final int RECOMMEND_TYPE_PERSONALIZED = 0;
    private static final Pattern GRADE_PATTERN = Pattern.compile("(20\\d{2})");

    private final SysUserRepository sysUserRepository;
    private final BookInfoRepository bookInfoRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final RecommendationConfigRepository recommendationConfigRepository;
    private final RecommendationListRepository recommendationListRepository;
    private final OperationLogRepository operationLogRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.recommendation.python-command:python}")
    private String pythonCommand;

    @Value("${app.recommendation.python-script:recommendation/hybrid_recommender.py}")
    private String pythonScript;

    @Value("${app.recommendation.default-top-n:20}")
    private int defaultTopN;

    public HybridRecommendationService(
        SysUserRepository sysUserRepository,
        BookInfoRepository bookInfoRepository,
        BorrowRecordRepository borrowRecordRepository,
        RecommendationConfigRepository recommendationConfigRepository,
        RecommendationListRepository recommendationListRepository,
        OperationLogRepository operationLogRepository,
        ObjectMapper objectMapper) {
        this.sysUserRepository = sysUserRepository;
        this.bookInfoRepository = bookInfoRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.recommendationConfigRepository = recommendationConfigRepository;
        this.recommendationListRepository = recommendationListRepository;
        this.operationLogRepository = operationLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public int generateForUser(Long userId, Integer topN, Long operatorId) {
        if (userId == null) {
            return 0;
        }

        Optional<SysUser> userOptional = sysUserRepository.findById(userId);
        if (userOptional.isEmpty() || userOptional.get().getRole() == null || userOptional.get().getRole() != ROLE_STUDENT) {
            return 0;
        }

        int limit = resolveTopN(topN);
        List<PythonRecommendationItem> recommendationItems;

        try {
            recommendationItems = runPythonHybridRecommend(userId, limit);
        } catch (Exception ex) {
            LOGGER.warn("Python hybrid recommendation failed, fallback to trending. userId={}", userId, ex);
            recommendationItems = fallbackRecommendations(userId, limit);
        }

        List<RecommendationList> entities = recommendationItems.stream()
            .map(item -> toEntity(userId, item))
            .collect(Collectors.toList());

        recommendationListRepository.deleteByUserIdAndRecommendType(userId, RECOMMEND_TYPE_PERSONALIZED);
        if (!entities.isEmpty()) {
            recommendationListRepository.saveAll(entities);
        }

        recordRecommendLog(operatorId == null ? userId : operatorId, userId, entities.size(), "HYBRID");
        return entities.size();
    }

    @Transactional
    public int generateForAllStudents(Integer topN, Long operatorId) {
        List<SysUser> students = sysUserRepository.findAll().stream()
            .filter(user -> user.getRole() != null && user.getRole() == ROLE_STUDENT)
            .collect(Collectors.toList());

        int totalGenerated = 0;
        for (SysUser student : students) {
            totalGenerated += generateForUser(student.getUserId(), topN, operatorId);
        }
        return totalGenerated;
    }

    private List<PythonRecommendationItem> runPythonHybridRecommend(Long userId, int topN)
        throws IOException, InterruptedException {
        Map<String, Object> payload = buildPythonPayload(userId, topN);

        Path scriptPath = Paths.get(pythonScript);
        if (!scriptPath.isAbsolute()) {
            scriptPath = Paths.get(System.getProperty("user.dir")).resolve(scriptPath).normalize();
        }

        ProcessBuilder processBuilder = new ProcessBuilder(pythonCommand, scriptPath.toString());
        Process process = processBuilder.start();

        byte[] inputBytes = objectMapper.writeValueAsBytes(payload);
        process.getOutputStream().write(inputBytes);
        process.getOutputStream().flush();
        process.getOutputStream().close();

        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IllegalStateException("Python process exited with code " + exitCode + ", stderr=" + stderr);
        }

        if (stdout == null || stdout.isBlank()) {
            return new ArrayList<>();
        }

        Map<String, Object> result = objectMapper.readValue(stdout, new TypeReference<Map<String, Object>>() {
        });
        Object recommendationObj = result.get("recommendations");
        if (!(recommendationObj instanceof List<?> recommendationList)) {
            return new ArrayList<>();
        }

        List<PythonRecommendationItem> items = new ArrayList<>();
        for (Object item : recommendationList) {
            if (!(item instanceof Map<?, ?> data)) {
                continue;
            }

            Number bookIdNum = toNumber(data.get("book_id"));
            Number scoreNum = toNumber(data.get("score"));
            if (bookIdNum == null || scoreNum == null) {
                continue;
            }

            PythonRecommendationItem recommendationItem = new PythonRecommendationItem();
            recommendationItem.setBookId(bookIdNum.longValue());
            recommendationItem.setScore(BigDecimal.valueOf(scoreNum.doubleValue()));
            Object reason = data.containsKey("reason") ? data.get("reason") : "混合推荐结果";
            recommendationItem.setReason(reason == null ? "混合推荐结果" : String.valueOf(reason));
            items.add(recommendationItem);
        }

        return items;
    }

    private Number toNumber(Object value) {
        if (value instanceof Number number) {
            return number;
        }
        if (value instanceof String strValue && !strValue.isBlank()) {
            try {
                return new BigDecimal(strValue);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Map<String, Object> buildPythonPayload(Long userId, int topN) {
        List<SysUser> users = sysUserRepository.findAll();
        List<BookInfo> books = bookInfoRepository.findAll();
        List<BorrowRecord> borrowRecords = borrowRecordRepository.findAll();
        RecommendationConfig config = resolveConfig();

        List<Map<String, Object>> userPayload = users.stream()
            .map(user -> {
                Map<String, Object> item = new HashMap<>();
                item.put("user_id", user.getUserId());
                item.put("username", user.getUsername());
                item.put("dept_name", user.getDeptName());
                item.put("grade", parseGrade(user.getUsername()));
                item.put("role", user.getRole());
                return item;
            })
            .collect(Collectors.toList());

        List<Map<String, Object>> bookPayload = books.stream()
            .map(book -> {
                Map<String, Object> item = new HashMap<>();
                item.put("book_id", book.getBookId());
                item.put("title", book.getTitle());
                item.put("author", book.getAuthor());
                item.put("category", book.getCategory());
                item.put("description", book.getDescription());
                return item;
            })
            .collect(Collectors.toList());

        List<Map<String, Object>> borrowPayload = borrowRecords.stream()
            .map(record -> {
                Map<String, Object> item = new HashMap<>();
                item.put("user_id", record.getUserId());
                item.put("book_id", record.getBookId());
                return item;
            })
            .collect(Collectors.toList());

        Map<String, Object> configPayload = new LinkedHashMap<>();
        configPayload.put("collaborative_weight", safeWeight(config.getCollaborativeWeight(), new BigDecimal("0.45")));
        configPayload.put("content_weight", safeWeight(config.getPersonalizedWeight(), new BigDecimal("0.35")));
        configPayload.put("profile_weight", safeWeight(config.getTrendingWeight(), new BigDecimal("0.20")));
        configPayload.put("min_similarity", safeWeight(config.getMinSimilarity(), new BigDecimal("0.10")));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("target_user_id", userId);
        payload.put("top_n", topN);
        payload.put("users", userPayload);
        payload.put("books", bookPayload);
        payload.put("borrows", borrowPayload);
        payload.put("config", configPayload);
        return payload;
    }

    private RecommendationConfig resolveConfig() {
        return recommendationConfigRepository.findAll().stream()
            .findFirst()
            .orElseGet(() -> {
                RecommendationConfig defaultConfig = new RecommendationConfig();
                defaultConfig.setCollaborativeWeight(new BigDecimal("0.45"));
                defaultConfig.setPersonalizedWeight(new BigDecimal("0.35"));
                defaultConfig.setTrendingWeight(new BigDecimal("0.20"));
                defaultConfig.setMinSimilarity(new BigDecimal("0.10"));
                defaultConfig.setColdStartStrategy("TRENDING");
                return defaultConfig;
            });
    }

    private String parseGrade(String username) {
        if (username == null) {
            return "";
        }
        Matcher matcher = GRADE_PATTERN.matcher(username);
        return matcher.find() ? matcher.group(1) : "";
    }

    private BigDecimal safeWeight(BigDecimal value, BigDecimal fallback) {
        if (value == null) {
            return fallback;
        }
        return value;
    }

    private RecommendationList toEntity(Long userId, PythonRecommendationItem item) {
        RecommendationList entity = new RecommendationList();
        entity.setUserId(userId);
        entity.setBookId(item.getBookId());
        entity.setRecommendType(RECOMMEND_TYPE_PERSONALIZED);
        entity.setRecommendReason(item.getReason());
        entity.setScore(item.getScore().setScale(4, RoundingMode.HALF_UP));
        entity.setCreateTime(LocalDateTime.now());
        return entity;
    }

    private List<PythonRecommendationItem> fallbackRecommendations(Long userId, int topN) {
        List<BorrowRecord> records = borrowRecordRepository.findAll();
        List<Long> borrowedBookIds = borrowRecordRepository.findByUserId(userId).stream()
            .map(BorrowRecord::getBookId)
            .collect(Collectors.toList());

        Map<Long, Long> popularityMap = records.stream()
            .collect(Collectors.groupingBy(BorrowRecord::getBookId, Collectors.counting()));

        return popularityMap.entrySet().stream()
            .filter(entry -> !borrowedBookIds.contains(entry.getKey()))
            .sorted((left, right) -> Long.compare(right.getValue(), left.getValue()))
            .limit(topN)
            .map(entry -> {
                PythonRecommendationItem item = new PythonRecommendationItem();
                item.setBookId(entry.getKey());
                item.setScore(BigDecimal.valueOf(entry.getValue()));
                item.setReason("热门借阅回退推荐");
                return item;
            })
            .collect(Collectors.toList());
    }

    private void recordRecommendLog(Long operatorId, Long userId, int recommendationCount, String source) {
        OperationLog log = new OperationLog();
        log.setUserId(operatorId == null ? userId : operatorId);
        log.setOperationType("RECOMMENDATION_GENERATE");
        log.setTargetId(userId);
        log.setOldValue("source=" + source);
        log.setNewValue("count=" + recommendationCount);
        log.setCreateTime(LocalDateTime.now());
        operationLogRepository.save(log);
    }

    private int resolveTopN(Integer topN) {
        if (topN == null || topN <= 0) {
            return defaultTopN;
        }
        return Math.min(topN, 100);
    }

    private static class PythonRecommendationItem {
        private Long bookId;
        private BigDecimal score;
        private String reason;

        public Long getBookId() {
            return bookId;
        }

        public void setBookId(Long bookId) {
            this.bookId = bookId;
        }

        public BigDecimal getScore() {
            return score;
        }

        public void setScore(BigDecimal score) {
            this.score = score;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
