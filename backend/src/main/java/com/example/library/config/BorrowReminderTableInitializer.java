package com.example.library.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 启动时确保借阅提醒表存在，避免旧数据库缺表导致提醒接口500。
 */
@Component
public class BorrowReminderTableInitializer {

    private final JdbcTemplate jdbcTemplate;

    public BorrowReminderTableInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureBorrowReminderTableExists() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS borrow_reminder (
                reminder_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '提醒ID',
                user_id BIGINT NOT NULL COMMENT '用户ID',
                record_id BIGINT NOT NULL COMMENT '借阅记录ID',
                book_title VARCHAR(100) NOT NULL COMMENT '图书标题',
                reminder_type INT NOT NULL COMMENT '提醒类型：0=即将逾期，1=已逾期，2=罚款提醒',
                message VARCHAR(255) NOT NULL COMMENT '提醒消息',
                due_date DATETIME NOT NULL COMMENT '应还日期',
                overday_count INT COMMENT '逾期天数',
                fine_amount DECIMAL(8, 2) COMMENT '罚款金额',
                is_read INT NOT NULL DEFAULT 0 COMMENT '是否已读：0=未读，1=已读',
                notification_channels VARCHAR(50) NOT NULL DEFAULT 'IN_APP,EMAIL' COMMENT '通知渠道',
                in_app_notified INT NOT NULL DEFAULT 1 COMMENT '站内信通知状态：0=未发送，1=已发送',
                email_notified INT NOT NULL DEFAULT 1 COMMENT '邮件通知状态：0=未发送，1=已发送',
                create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                CONSTRAINT fk_borrow_reminder_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE,
                CONSTRAINT fk_borrow_reminder_record FOREIGN KEY (record_id) REFERENCES borrow_record(record_id) ON DELETE CASCADE,
                UNIQUE KEY uk_user_record_type (user_id, record_id, reminder_type),
                INDEX idx_user_read_time (user_id, is_read, create_time)
            ) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
            """);
    }
}
