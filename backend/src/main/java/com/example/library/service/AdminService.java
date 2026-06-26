package com.example.library.service;

import com.example.library.dto.BorrowStatisticsResponse;
import com.example.library.dto.AdminRenewRequestResponse;
import com.example.library.dto.UserStatisticsResponse;
import com.example.library.entity.BookInfo;
import com.example.library.entity.BookRenewRequest;
import com.example.library.entity.BorrowRecord;
import com.example.library.dto.RecommendationConfigResponse;
import com.example.library.entity.OperationLog;
import com.example.library.entity.RecommendationConfig;
import com.example.library.entity.SysUser;
import com.example.library.repository.BookInfoRepository;
import com.example.library.repository.BookRenewRequestRepository;
import com.example.library.repository.BorrowRecordRepository;
import com.example.library.repository.OperationLogRepository;
import com.example.library.repository.RecommendationConfigRepository;
import com.example.library.repository.SysUserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 管理员服务
 */
@Service
public class AdminService {

    private static final int ROLE_ADMIN = 0;
    private static final int ROLE_TEACHER = 1;
    private static final int ROLE_STUDENT = 2;
    private static final int TREND_DAYS = 7;

    private final SysUserRepository sysUserRepository;
    private final BookInfoRepository bookInfoRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final OperationLogRepository operationLogRepository;
    private final RecommendationConfigRepository recommendationConfigRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookRenewRequestRepository bookRenewRequestRepository;
    private final TeacherService teacherService;
    private final HybridRecommendationService hybridRecommendationService;

    public AdminService(
        SysUserRepository sysUserRepository,
        BookInfoRepository bookInfoRepository,
        BorrowRecordRepository borrowRecordRepository,
        OperationLogRepository operationLogRepository,
        RecommendationConfigRepository recommendationConfigRepository,
        PasswordEncoder passwordEncoder,
        BookRenewRequestRepository bookRenewRequestRepository,
        TeacherService teacherService,
        HybridRecommendationService hybridRecommendationService) {
        this.sysUserRepository = sysUserRepository;
        this.bookInfoRepository = bookInfoRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.operationLogRepository = operationLogRepository;
        this.recommendationConfigRepository = recommendationConfigRepository;
        this.passwordEncoder = passwordEncoder;
        this.bookRenewRequestRepository = bookRenewRequestRepository;
        this.teacherService = teacherService;
        this.hybridRecommendationService = hybridRecommendationService;
    }

    public List<AdminRenewRequestResponse> getRenewRequests(Integer status) {
        List<BookRenewRequest> requests = status == null
            ? bookRenewRequestRepository.findAll()
            : bookRenewRequestRepository.findByStatusOrderByRequestDateDesc(status);

        return requests.stream()
            .sorted((left, right) -> right.getRequestDate().compareTo(left.getRequestDate()))
            .map(request -> {
                BorrowRecord record = borrowRecordRepository.findById(request.getRecordId()).orElse(null);
                SysUser user = sysUserRepository.findById(request.getUserId()).orElse(null);
                BookInfo book = null;
                if (record != null) {
                    book = bookInfoRepository.findById(record.getBookId()).orElse(null);
                }

                return new AdminRenewRequestResponse(
                    request.getRenewId(),
                    request.getRecordId(),
                    request.getUserId(),
                    user != null ? user.getUsername() : "",
                    user != null ? user.getRealName() : "",
                    book != null ? book.getTitle() : "",
                    book != null ? book.getAuthor() : "",
                    record != null ? record.getDueDate() : null,
                    request.getRequestDate(),
                    request.getStatus(),
                    request.getNewDueDate(),
                    request.getReviewRemarks()
                );
            })
            .collect(Collectors.toList());
    }

    public boolean approveRenewRequest(Long renewId, Long adminId) {
        return teacherService.approveRenewal(renewId, adminId);
    }

    public boolean rejectRenewRequest(Long renewId, Long adminId, String reason) {
        return teacherService.rejectRenewal(renewId, reason, adminId);
    }

    /**
     * 获取所有用户列表
     */
    public Page<SysUser> getAllUsers(int page, int size, String keyword) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), size);
        if (keyword != null && !keyword.isEmpty()) {
            return sysUserRepository.findByUsernameContainingOrRealNameContainingOrderByUserIdDesc(keyword, keyword, pageable);
        }
        return sysUserRepository.findAllByOrderByUserIdDesc(pageable);
    }

    /**
     * 获取用户详情
     */
    public Optional<SysUser> getUserDetail(Long userId) {
        return sysUserRepository.findById(userId);
    }

    /**
     * 冻结用户
     */
    public boolean freezeUser(Long userId, Long adminId) {
        Optional<SysUser> optionalUser = sysUserRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return false;
        }

        SysUser user = optionalUser.get();
        user.setStatus(0);
        sysUserRepository.save(user);

        // 记录操作日志
        recordOperationLog(adminId, "FREEZE_USER", userId, "status", "1", "0");
        return true;
    }

    /**
     * 解冻用户
     */
    public boolean unfreezeUser(Long userId, Long adminId) {
        Optional<SysUser> optionalUser = sysUserRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return false;
        }

        SysUser user = optionalUser.get();
        user.setStatus(1);
        sysUserRepository.save(user);

        // 记录操作日志
        recordOperationLog(adminId, "UNFREEZE_USER", userId, "status", "0", "1");
        return true;
    }

    /**
     * 重置密码
     */
    public boolean resetPassword(Long userId, String newPassword, Long adminId) {
        Optional<SysUser> optionalUser = sysUserRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return false;
        }

        SysUser user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserRepository.save(user);

        // 记录操作日志
        recordOperationLog(adminId, "RESET_PASSWORD", userId, "password", "[hidden]", "[hidden]");
        return true;
    }

    /**
     * 修改用户角色
     */
    public boolean changeUserRole(Long userId, Integer newRole, Long adminId) {
        Optional<SysUser> optionalUser = sysUserRepository.findById(userId);
        if (optionalUser.isEmpty() || newRole < 0 || newRole > 2) {
            return false;
        }

        SysUser user = optionalUser.get();
        Integer oldRole = user.getRole();
        user.setRole(newRole);
        user.setMaxLimit(resolveMaxLimit(newRole));
        sysUserRepository.save(user);

        // 记录操作日志
        recordOperationLog(adminId, "CHANGE_ROLE", userId, "role", oldRole.toString(), newRole.toString());
        return true;
    }

    /**
     * 获取借阅统计
     */
    public BorrowStatisticsResponse getBorrowStatistics() {
        LocalDateTime now = LocalDateTime.now();
        List<BorrowRecord> records = borrowRecordRepository.findAll();
        Map<Long, BookInfo> bookMap = bookInfoRepository.findAll().stream()
            .collect(Collectors.toMap(BookInfo::getBookId, book -> book));
        Map<Long, SysUser> userMap = sysUserRepository.findAll().stream()
            .collect(Collectors.toMap(SysUser::getUserId, user -> user));

        long totalBorrowCount = records.size();
        long overdueBookCount = records.stream()
            .filter(record -> isOverdue(record, now))
            .count();
        long overdueUserCount = records.stream()
            .filter(record -> isOverdue(record, now))
            .map(BorrowRecord::getUserId)
            .distinct()
            .count();

        BigDecimal totalFineAmount = records.stream()
            .map(BorrowRecord::getFine)
            .filter(fine -> fine != null && fine.compareTo(BigDecimal.ZERO) > 0)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int unpaidFineCount = (int) records.stream()
            .filter(record -> record.getFine() != null && record.getFine().compareTo(BigDecimal.ZERO) > 0)
            .count();

        BigDecimal dailyAverageBorrow = calculateDailyAverageBorrow(records);
        BigDecimal overdueRate = calculateRate(overdueBookCount, totalBorrowCount);
        BigDecimal repeatBorrowRate = calculateRepeatBorrowRate(records);

        List<Map<String, Object>> borrowTrend = calculateBorrowTrend(records);
        List<Map<String, Object>> categoryDistribution = calculateCategoryDistribution(records, bookMap);
        List<Map<String, Object>> activeUsers = calculateActiveUsers(records, userMap);
        List<Map<String, Object>> overdueRanking = calculateOverdueRanking(records, userMap, now);

        BorrowStatisticsResponse response = BorrowStatisticsResponse.builder()
            .totalBorrowCount(totalBorrowCount)
            .dailyAverageBorrow(dailyAverageBorrow)
            .overdueRate(overdueRate)
            .repeatBorrowRate(repeatBorrowRate)
            .borrowTrend(borrowTrend)
            .categoryDistribution(categoryDistribution)
            .activeUsers(activeUsers)
            .overdueUserCount(overdueUserCount)
            .overdueBookCount(overdueBookCount)
            .overdueRanking(overdueRanking)
            .totalFineAmount(totalFineAmount)
            .unpaidFineCount(unpaidFineCount)
            .build();

        return response;
    }

    public UserStatisticsResponse getUserStatistics() {
        long totalUserCount = sysUserRepository.count();
        long adminCount = sysUserRepository.countByRole(ROLE_ADMIN);
        long teacherCount = sysUserRepository.countByRole(ROLE_TEACHER);
        long studentCount = sysUserRepository.countByRole(ROLE_STUDENT);

        return UserStatisticsResponse.builder()
            .totalUserCount(totalUserCount)
            .adminCount(adminCount)
            .teacherCount(teacherCount)
            .studentCount(studentCount)
            .build();
    }

    private BigDecimal calculateDailyAverageBorrow(List<BorrowRecord> records) {
        if (records.isEmpty()) {
            return BigDecimal.ZERO;
        }

        LocalDate minDate = records.stream()
            .map(BorrowRecord::getBorrowDate)
            .filter(date -> date != null)
            .min(LocalDateTime::compareTo)
            .map(LocalDateTime::toLocalDate)
            .orElse(LocalDate.now());

        LocalDate maxDate = records.stream()
            .map(BorrowRecord::getBorrowDate)
            .filter(date -> date != null)
            .max(LocalDateTime::compareTo)
            .map(LocalDateTime::toLocalDate)
            .orElse(LocalDate.now());

        long daySpan = Math.max(1, maxDate.toEpochDay() - minDate.toEpochDay() + 1);
        return BigDecimal.valueOf(records.size())
            .divide(BigDecimal.valueOf(daySpan), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRate(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(numerator)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRepeatBorrowRate(List<BorrowRecord> records) {
        if (records.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Map<Long, Long> userBorrowCountMap = records.stream()
            .collect(Collectors.groupingBy(BorrowRecord::getUserId, Collectors.counting()));

        long totalUsers = userBorrowCountMap.size();
        long repeatBorrowUsers = userBorrowCountMap.values().stream()
            .filter(count -> count > 1)
            .count();

        return calculateRate(repeatBorrowUsers, totalUsers);
    }

    private List<Map<String, Object>> calculateBorrowTrend(List<BorrowRecord> records) {
        LocalDate startDate = LocalDate.now().minusDays(TREND_DAYS - 1L);

        Map<LocalDate, Long> borrowCountByDate = records.stream()
            .map(BorrowRecord::getBorrowDate)
            .filter(dateTime -> dateTime != null)
            .map(LocalDateTime::toLocalDate)
            .filter(date -> !date.isBefore(startDate))
            .collect(Collectors.groupingBy(date -> date, Collectors.counting()));

        List<Map<String, Object>> trend = new ArrayList<>();
        for (int day = 0; day < TREND_DAYS; day++) {
            LocalDate date = startDate.plusDays(day);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.toString());
            item.put("count", borrowCountByDate.getOrDefault(date, 0L));
            trend.add(item);
        }
        return trend;
    }

    private List<Map<String, Object>> calculateCategoryDistribution(
        List<BorrowRecord> records,
        Map<Long, BookInfo> bookMap) {
        Map<String, Long> categoryCountMap = records.stream()
            .map(record -> bookMap.get(record.getBookId()))
            .filter(book -> book != null)
            .map(book -> book.getCategory() == null ? "未分类" : book.getCategory())
            .collect(Collectors.groupingBy(category -> category, Collectors.counting()));

        return categoryCountMap.entrySet().stream()
            .sorted((left, right) -> Long.compare(right.getValue(), left.getValue()))
            .map(entry -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("category", entry.getKey());
                item.put("count", entry.getValue());
                return item;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> calculateActiveUsers(
        List<BorrowRecord> records,
        Map<Long, SysUser> userMap) {
        Map<Long, Long> userBorrowCountMap = records.stream()
            .collect(Collectors.groupingBy(BorrowRecord::getUserId, Collectors.counting()));

        return userBorrowCountMap.entrySet().stream()
            .sorted((left, right) -> Long.compare(right.getValue(), left.getValue()))
            .limit(10)
            .map(entry -> {
                SysUser user = userMap.get(entry.getKey());
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("userId", entry.getKey());
                item.put("username", user != null ? user.getUsername() : "未知用户");
                item.put("realName", user != null ? user.getRealName() : "未知");
                item.put("borrowCount", entry.getValue());
                return item;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> calculateOverdueRanking(
        List<BorrowRecord> records,
        Map<Long, SysUser> userMap,
        LocalDateTime now) {
        Map<Long, List<BorrowRecord>> overdueUserRecords = records.stream()
            .filter(record -> isOverdue(record, now))
            .collect(Collectors.groupingBy(BorrowRecord::getUserId));

        return overdueUserRecords.entrySet().stream()
            .map(entry -> {
                Long userId = entry.getKey();
                List<BorrowRecord> userRecords = entry.getValue();
                SysUser user = userMap.get(userId);

                long overdueCount = userRecords.size();
                long maxOverdueDays = userRecords.stream()
                    .mapToLong(record -> Math.max(0,
                        java.time.Duration.between(record.getDueDate(), now).toDays()))
                    .max()
                    .orElse(0L);

                BigDecimal fineAmount = userRecords.stream()
                    .map(BorrowRecord::getFine)
                    .filter(fine -> fine != null && fine.compareTo(BigDecimal.ZERO) > 0)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("userId", userId);
                item.put("username", user != null ? user.getUsername() : "未知用户");
                item.put("realName", user != null ? user.getRealName() : "未知");
                item.put("overdueCount", overdueCount);
                item.put("maxOverdueDays", maxOverdueDays);
                item.put("fineAmount", fineAmount);
                return item;
            })
            .sorted(Comparator
                .comparing((Map<String, Object> item) -> (Long) item.get("overdueCount"))
                .thenComparing(item -> (Long) item.get("maxOverdueDays"))
                .reversed())
            .limit(10)
            .collect(Collectors.toList());
    }

    private boolean isOverdue(BorrowRecord record, LocalDateTime now) {
        if (record == null || record.getDueDate() == null) {
            return false;
        }
        return record.getStatus() != null && record.getStatus() == 2
            || (record.getReturnDate() == null && record.getDueDate().isBefore(now));
    }

    /**
     * 获取推荐配置
     */
    public Optional<RecommendationConfigResponse> getRecommendationConfig() {
        List<RecommendationConfig> configs = recommendationConfigRepository.findAll();
        if (configs.isEmpty()) {
            return Optional.empty();
        }

        RecommendationConfig config = configs.get(0);
        RecommendationConfigResponse response = new RecommendationConfigResponse(
            config.getConfigId(),
            config.getCollaborativeWeight(),
            config.getPersonalizedWeight(),
            config.getTrendingWeight(),
            config.getMinSimilarity(),
            config.getColdStartStrategy()
        );
        return Optional.of(response);
    }

    /**
     * 更新推荐配置
     */
    public boolean updateRecommendationConfig(RecommendationConfigResponse configRequest, Long adminId) {
        List<RecommendationConfig> configs = recommendationConfigRepository.findAll();
        RecommendationConfig config;

        if (configs.isEmpty()) {
            config = new RecommendationConfig();
        } else {
            config = configs.get(0);
        }

        config.setCollaborativeWeight(configRequest.getCollaborativeWeight());
        config.setPersonalizedWeight(configRequest.getPersonalizedWeight());
        config.setTrendingWeight(configRequest.getTrendingWeight());
        config.setMinSimilarity(configRequest.getMinSimilarity());
        config.setColdStartStrategy(configRequest.getColdStartStrategy());
        config.setUpdateTime(LocalDateTime.now());
        config.setUpdatedBy(adminId);

        recommendationConfigRepository.save(config);

        // 记录操作日志
        recordOperationLog(adminId, "UPDATE_RECOMMENDATION_CONFIG", config.getConfigId(), "config", "old", "new");
        return true;
    }

    /**
     * 获取操作日志
     */
    public Page<OperationLog> getOperationLogs(int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), size);
        return operationLogRepository.findAll(pageable);
    }

    /**
     * 记录操作日志
     */
    public void recordOperationLog(Long userId, String operationType, Long targetId, String fieldName, String oldValue, String newValue) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setOperationType(operationType);
        log.setTargetId(targetId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setCreateTime(LocalDateTime.now());
        operationLogRepository.save(log);
    }

    /**
     * 触发推荐重算（可按用户或全部学生）
     */
    public int rebuildRecommendations(Long userId, Integer topN, Long adminId) {
        if (userId != null) {
            return hybridRecommendationService.generateForUser(userId, topN, adminId);
        }
        return hybridRecommendationService.generateForAllStudents(topN, adminId);
    }

    private Integer resolveMaxLimit(Integer role) {
        if (role == null) {
            return 10;
        }
        if (role == ROLE_ADMIN) {
            return 100;
        }
        if (role == ROLE_TEACHER) {
            return 20;
        }
        return 10; // ROLE_STUDENT
    }
}
