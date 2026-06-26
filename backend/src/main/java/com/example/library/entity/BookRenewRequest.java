package com.example.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 续借申请表
 * 0:待审批, 1:已批准, 2:已拒绝
 */
@Data
@Entity
@Table(name = "book_renew_request")
public class BookRenewRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "renew_id")
    private Long renewId;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    @Column(name = "review_remarks", length = 255)
    private String reviewRemarks;

    @Column(name = "new_due_date")
    private LocalDateTime newDueDate;
}
