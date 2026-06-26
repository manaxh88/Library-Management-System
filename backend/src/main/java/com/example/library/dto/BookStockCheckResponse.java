package com.example.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookStockCheckResponse {
    private Long bookId;
    private String isbn;
    private String title;
    private String author;
    private Integer totalStock;
    private Integer availableStock;
    private boolean canBorrow;
}
