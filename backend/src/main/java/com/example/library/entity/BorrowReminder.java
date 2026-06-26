package com.example.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "borrow_reminder")
public class BorrowReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reminder_id")
    private Long reminderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "book_title", nullable = false, length = 100)
    private String bookTitle;

    @Column(name = "reminder_type", nullable = false)
    private Integer reminderType;

    @Column(name = "message", nullable = false, length = 255)
    private String message;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "overday_count")
    private Integer overdayCount;

    @Column(name = "fine_amount", precision = 8, scale = 2)
    private BigDecimal fineAmount;

    @Column(name = "is_read", nullable = false)
    private Integer isRead;

    @Column(name = "notification_channels", nullable = false, length = 50)
    private String notificationChannels;

    @Column(name = "in_app_notified", nullable = false)
    private Integer inAppNotified;

    @Column(name = "email_notified", nullable = false)
    private Integer emailNotified;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
}
