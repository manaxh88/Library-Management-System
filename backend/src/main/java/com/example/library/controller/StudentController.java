package com.example.library.controller;

import com.example.library.dto.ApiResponse;
import com.example.library.dto.BorrowReminderResponse;
import com.example.library.dto.RecommendationResponse;
import com.example.library.service.StudentService;
import com.example.library.util.RequireRole;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生控制器
 */
@RestController
@RequestMapping({"/api/student", "/student"})
@RequireRole(roles = {2})  // 仅学生可访问
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * 获取个性化推荐
     */
    @GetMapping("/recommendations")
    public ApiResponse<Page<RecommendationResponse>> getPersonalizedRecommendations(
        @RequestParam Long userId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {
        Page<RecommendationResponse> recommendations = studentService.getPersonalizedRecommendations(userId, page, size);
        return ApiResponse.success(recommendations);
    }

    /**
     * 获取关联推荐
     */
    @GetMapping("/recommendations/related")
    public ApiResponse<List<RecommendationResponse>> getRelatedRecommendations(
        @RequestParam Long userId,
        @RequestParam Long bookId) {
        List<RecommendationResponse> recommendations = studentService.getRelatedRecommendations(userId, bookId);
        return ApiResponse.success(recommendations);
    }

    /**
     * 获取借阅提醒
     */
    @GetMapping("/reminders")
    public ApiResponse<List<BorrowReminderResponse>> getBorrowReminders(
        @RequestParam Long userId) {
        List<BorrowReminderResponse> reminders = studentService.getBorrowReminders(userId);
        return ApiResponse.success(reminders);
    }

    /**
     * 标记提醒为已读
     */
    @PutMapping("/reminders/{reminderId}/read")
    public ApiResponse<Boolean> markReminderAsRead(
        @RequestParam Long userId,
        @PathVariable Long reminderId) {
        boolean updated = studentService.markReminderAsRead(userId, reminderId);
        return ApiResponse.success(updated);
    }

    /**
     * 全部标记为已读
     */
    @PutMapping("/reminders/read-all")
    public ApiResponse<Integer> markAllRemindersAsRead(
        @RequestParam Long userId) {
        int updatedCount = studentService.markAllRemindersAsRead(userId);
        return ApiResponse.success(updatedCount);
    }
}
