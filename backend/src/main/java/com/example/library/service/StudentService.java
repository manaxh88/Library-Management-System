package com.example.library.service;

import com.example.library.dto.BorrowReminderResponse;
import com.example.library.dto.RecommendationResponse;
import com.example.library.entity.BookInfo;
import com.example.library.entity.BorrowReminder;
import com.example.library.entity.BorrowRecord;
import com.example.library.entity.RecommendationList;
import com.example.library.repository.BookInfoRepository;
import com.example.library.repository.BorrowReminderRepository;
import com.example.library.repository.BorrowRecordRepository;
import com.example.library.repository.RecommendationListRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 学生服务
 */
@Service
public class StudentService {

    private static final int REMINDER_TYPE_DUE_SOON = 0;
    private static final int REMINDER_TYPE_OVERDUE = 1;
    private static final String DEFAULT_NOTIFICATION_CHANNELS = "IN_APP,EMAIL";

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookInfoRepository bookInfoRepository;
    private final RecommendationListRepository recommendationListRepository;
    private final BorrowReminderRepository borrowReminderRepository;
    private final HybridRecommendationService hybridRecommendationService;

    public StudentService(
        BorrowRecordRepository borrowRecordRepository,
        BookInfoRepository bookInfoRepository,
        RecommendationListRepository recommendationListRepository,
        BorrowReminderRepository borrowReminderRepository,
        HybridRecommendationService hybridRecommendationService) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookInfoRepository = bookInfoRepository;
        this.recommendationListRepository = recommendationListRepository;
        this.borrowReminderRepository = borrowReminderRepository;
        this.hybridRecommendationService = hybridRecommendationService;
    }

    /**
     * 获取个性化推荐（学生）
     */
    public Page<RecommendationResponse> getPersonalizedRecommendations(Long userId, int page, int size) {
        hybridRecommendationService.generateForUser(userId, Math.max(size * 3, 20), userId);
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), size);
        return recommendationListRepository.findByUserIdOrderByScoreDescCreateTimeDesc(userId, pageable)
            .map(this::toRecommendationResponse);
    }

    /**
     * 获取关联推荐
     */
    public List<RecommendationResponse> getRelatedRecommendations(Long userId, Long bookId) {
        List<RecommendationList> recommendations = recommendationListRepository.findByUserIdAndRecommendType(userId, 2);
        return recommendations.stream()
            .map(this::toRecommendationResponse)
            .collect(Collectors.toList());
    }

    /**
     * 获取借阅提醒
     */
    @Transactional
    public List<BorrowReminderResponse> getBorrowReminders(Long userId) {
        syncBorrowReminders(userId);
        return borrowReminderRepository.findByUserIdOrderByIsReadAscCreateTimeDesc(userId)
            .stream()
            .map(this::toBorrowReminderResponse)
            .collect(Collectors.toList());
    }

    /**
     * 标记提醒为已读
     */
    @Transactional
    public boolean markReminderAsRead(Long userId, Long reminderId) {
        return borrowReminderRepository.markReadByIdAndUserId(reminderId, userId) > 0;
    }

    /**
     * 全部标记为已读
     */
    @Transactional
    public int markAllRemindersAsRead(Long userId) {
        return borrowReminderRepository.markAllReadByUserId(userId);
    }

    /**
     * 计算罚款金额
     * 规则：0.5元/天/本，上限10元
     */
    private BigDecimal calculateFine(long overdayCount) {
        BigDecimal finePerDay = new BigDecimal("0.5");
        BigDecimal totalFine = finePerDay.multiply(new BigDecimal(overdayCount));
        BigDecimal maxFine = new BigDecimal("10");

        if (totalFine.compareTo(maxFine) > 0) {
            totalFine = maxFine;
        }

        return totalFine;
    }

    private void syncBorrowReminders(Long userId) {
        List<BorrowRecord> records = borrowRecordRepository.findByUserIdAndReturnDateIsNull(userId);
        LocalDate today = LocalDate.now();

        for (BorrowRecord record : records) {
            if (record == null || record.getDueDate() == null) {
                continue;
            }

            long daysLeft = ChronoUnit.DAYS.between(today, record.getDueDate().toLocalDate());

            if (daysLeft < 0) {
                int overdayCount = (int) Math.abs(daysLeft);
                String bookTitle = bookInfoRepository.findById(record.getBookId())
                    .map(BookInfo::getTitle)
                    .orElse("未知图书");
                upsertReminder(
                    userId,
                    record,
                    bookTitle,
                    REMINDER_TYPE_OVERDUE,
                    "您有一本图书《" + bookTitle + "》已逾期" + overdayCount + "天，请尽快归还",
                    overdayCount,
                    calculateFine(overdayCount)
                );
            } else if (daysLeft < 7) {
                String bookTitle = bookInfoRepository.findById(record.getBookId())
                    .map(BookInfo::getTitle)
                    .orElse("未知图书");
                upsertReminder(
                    userId,
                    record,
                    bookTitle,
                    REMINDER_TYPE_DUE_SOON,
                    "您有一本图书《" + bookTitle + "》将在" + daysLeft + "天后逾期，请尽快归还或续借",
                    null,
                    null
                );
            }
        }
    }

    private void upsertReminder(
        Long userId,
        BorrowRecord record,
        String bookTitle,
        Integer reminderType,
        String message,
        Integer overdayCount,
        BigDecimal fineAmount) {
        LocalDateTime now = LocalDateTime.now();
        BorrowReminder reminder = borrowReminderRepository
            .findFirstByUserIdAndRecordIdAndReminderTypeOrderByReminderIdDesc(
                userId,
                record.getRecordId(),
                reminderType)
            .orElseGet(BorrowReminder::new);

        boolean isNew = reminder.getReminderId() == null;
        reminder.setUserId(userId);
        reminder.setRecordId(record.getRecordId());
        reminder.setBookTitle(bookTitle);
        reminder.setReminderType(reminderType);
        reminder.setMessage(message);
        reminder.setDueDate(record.getDueDate());
        reminder.setOverdayCount(overdayCount);
        reminder.setFineAmount(fineAmount);
        reminder.setNotificationChannels(DEFAULT_NOTIFICATION_CHANNELS);
        reminder.setInAppNotified(1);
        reminder.setEmailNotified(1);
        reminder.setUpdateTime(now);
        if (isNew) {
            reminder.setIsRead(0);
            reminder.setCreateTime(now);
        }

        borrowReminderRepository.save(reminder);
    }

    private BorrowReminderResponse toBorrowReminderResponse(BorrowReminder reminder) {
        return BorrowReminderResponse.builder()
            .reminderId(reminder.getReminderId())
            .recordId(reminder.getRecordId())
            .bookTitle(reminder.getBookTitle())
            .reminderType(reminder.getReminderType())
            .message(reminder.getMessage())
            .dueDate(reminder.getDueDate())
            .overdayCount(reminder.getOverdayCount())
            .fineAmount(reminder.getFineAmount())
            .isRead(reminder.getIsRead() != null && reminder.getIsRead() == 1)
            .notificationChannels(parseNotificationChannels(reminder.getNotificationChannels()))
            .inAppNotified(reminder.getInAppNotified() != null && reminder.getInAppNotified() == 1)
            .emailNotified(reminder.getEmailNotified() != null && reminder.getEmailNotified() == 1)
            .createTime(reminder.getCreateTime())
            .build();
    }

    private List<String> parseNotificationChannels(String channels) {
        if (channels == null || channels.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(channels.split(","))
            .map(String::trim)
            .filter(item -> !item.isEmpty())
            .collect(Collectors.toList());
    }

    private RecommendationResponse toRecommendationResponse(RecommendationList recommendation) {
        Optional<BookInfo> bookOpt = bookInfoRepository.findById(recommendation.getBookId());
        if (bookOpt.isEmpty()) {
            return null;
        }

        BookInfo book = bookOpt.get();
        return new RecommendationResponse(
            recommendation.getRecommendId(),
            recommendation.getBookId(),
            book.getTitle(),
            book.getAuthor(),
            book.getCoverUrl(),
            recommendation.getRecommendReason(),
            recommendation.getScore(),
            recommendation.getRecommendType()
        );
    }
}
