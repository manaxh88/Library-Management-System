package com.example.library.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BookRequest {
    private String isbn;
    private String title;
    private String author;
    private String category;
    private String location;
    private Integer totalStock;
    private Integer availableStock;
    private String coverUrl;
    private LocalDateTime createTime;
}
