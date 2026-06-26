package com.example.library.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 借阅提醒响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowReminderResponse {
    private Long reminderId;
    private Long recordId;
    private String bookTitle;
    private Integer reminderType;  // 0:即将逾期, 1:已逾期, 2:罚款通知
    private String message;
    private LocalDateTime dueDate;
    private Integer overdayCount;  // 逾期天数
    private BigDecimal fineAmount; // 罚款金额
    private Boolean isRead;
    private List<String> notificationChannels; // 通知渠道：IN_APP, EMAIL
    private Boolean inAppNotified;
    private Boolean emailNotified;
    private LocalDateTime createTime;
}
