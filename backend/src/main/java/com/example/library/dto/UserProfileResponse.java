package com.example.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private Long userId;
    private String username;
    private String realName;
    private Integer role;
    private String deptName;
    private Integer maxLimit;
    private Integer status;
    private long currentBorrowCount;
    private long totalBorrowCount;
}
