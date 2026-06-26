package com.example.library.controller;

import com.example.library.dto.ApiResponse;
import com.example.library.dto.ChangePasswordRequest;
import com.example.library.dto.CreateUserRequest;
import com.example.library.dto.CreateUserResponse;
import com.example.library.dto.UserProfileResponse;
import com.example.library.service.UserService;
import com.example.library.util.RequireRole;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    @RequireRole(roles = {0})
    public ResponseEntity<ApiResponse<CreateUserResponse>> createUser(@RequestBody CreateUserRequest request) {
        Optional<CreateUserResponse> response = userService.createUser(request);
        if (response.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Username already exists"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response.get()));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(@PathVariable("id") Long id) {
        Optional<UserProfileResponse> response = userService.getUserProfile(id);
        if (response.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
        }
        return ResponseEntity.ok(ApiResponse.ok(response.get()));
    }

    @GetMapping("/users/username/{username}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfileByUsername(
            @PathVariable("username") String username) {
        Optional<UserProfileResponse> response = userService.getUserProfileByUsername(username);
        if (response.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
        }
        return ResponseEntity.ok(ApiResponse.ok(response.get()));
    }

    @PutMapping("/users/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@RequestBody ChangePasswordRequest request) {
        // Validate input
        if (request.getOldPassword() == null || request.getOldPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("旧密码不能为空"));
        }
        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("新密码不能为空"));
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("两次输入的密码不一致"));
        }
        if (request.getNewPassword().length() < 6) {
            return ResponseEntity.badRequest().body(ApiResponse.error("新密码至少需要6个字符"));
        }

        // Get current user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户未认证"));
        }

        String currentUsername = authentication.getName();
        Optional<UserProfileResponse> userProfile = userService.getUserProfileByUsername(currentUsername);
        if (userProfile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }

        Long userId = userProfile.get().getUserId();
        boolean success = userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());

        if (!success) {
            return ResponseEntity.badRequest().body(ApiResponse.error("旧密码不正确"));
        }

        return ResponseEntity.ok(ApiResponse.ok("密码修改成功"));
    }
}
