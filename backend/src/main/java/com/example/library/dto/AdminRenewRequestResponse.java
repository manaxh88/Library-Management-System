package com.example.library.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminRenewRequestResponse {
    private Long renewId;
    private Long recordId;
    private Long userId;
    private String username;
    private String realName;
    private String bookTitle;
    private String author;
    private LocalDateTime dueDate;
    private LocalDateTime requestDate;
    private Integer status;
    private LocalDateTime newDueDate;
    private String reviewRemarks;
}
