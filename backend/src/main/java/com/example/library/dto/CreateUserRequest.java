package com.example.library.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String realName;
    private Integer role;
    private String deptName;
}
