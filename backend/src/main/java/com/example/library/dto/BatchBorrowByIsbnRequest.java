package com.example.library.dto;

import java.util.List;
import lombok.Data;

@Data
public class BatchBorrowByIsbnRequest {
    private Long userId;
    private List<String> isbns;
}
