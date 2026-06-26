package com.example.library.dto;

import lombok.Data;

@Data
public class BorrowByIdentityRequest {
    private String username;
    private String isbn;
}
