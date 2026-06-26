package com.example.library.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 续借申请响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenewRequestResponse {
    private Long renewId;
    private Long recordId;
    private String bookTitle;
    private LocalDateTime currentDueDate;
    private LocalDateTime requestDate;
    private Integer status;  // 0:待审批, 1:已批准, 2:已拒绝
    private LocalDateTime reviewDate;
    private String reviewRemarks;
    private LocalDateTime newDueDate;
}
