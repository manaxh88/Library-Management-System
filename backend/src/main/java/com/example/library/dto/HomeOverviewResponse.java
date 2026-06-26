package com.example.library.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeOverviewResponse {
    private Long todayBorrowCount;
    private Long availableBookCount;
    private Long activeReaderCount;
    private List<ActivityItem> recentActivities;
    private List<BookItem> hotBooks;
    private List<BookItem> newBooks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActivityItem {
        private String type;
        private String title;
        private String content;
        private LocalDateTime time;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookItem {
        private Long bookId;
        private String isbn;
        private String title;
        private String author;
        private String category;
        private String location;
        private Integer totalStock;
        private Integer availableStock;
        private String coverUrl;
        private String publisher;
        private java.math.BigDecimal rating;
        private String description;
        private Long borrowCount;
    }
}
