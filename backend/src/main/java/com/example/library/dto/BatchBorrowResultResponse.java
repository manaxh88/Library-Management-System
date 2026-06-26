package com.example.library.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchBorrowResultResponse {
    private String receiptNo;
    private LocalDateTime createdAt;
    private Long userId;
    private int requestedCount;
    private int successCount;
    private int failedCount;
    private List<Item> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String isbn;
        private Long bookId;
        private String title;
        private String status;
        private String message;
        private Long recordId;
        private LocalDateTime dueDate;
    }
}
