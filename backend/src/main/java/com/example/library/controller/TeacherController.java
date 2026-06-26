package com.example.library.controller;

import com.example.library.dto.ApiResponse;
import com.example.library.dto.BatchBorrowRequest;
import com.example.library.dto.RecommendationResponse;
import com.example.library.entity.BookRenewRequest;
import com.example.library.service.TeacherService;
import com.example.library.util.RequireRole;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教师控制器
 */
@RestController
@RequestMapping({"/api/teacher", "/teacher"})
@RequireRole(roles = {1})  // 仅教师可访问
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    /**
     * 批量借阅（教师专用）
     */
    @PostMapping("/borrow/batch")
    public ApiResponse<String> batchBorrow(
        @RequestParam Long userId,
        @RequestBody BatchBorrowRequest request) {
        boolean success = teacherService.batchBorrow(userId, request.getBookIds());
        if (success) {
            return ApiResponse.success("批量借阅成功");
        }
        return ApiResponse.fail("批量借阅失败");
    }

    /**
     * 获取个性化推荐
     */
    @GetMapping("/recommendations")
    public ApiResponse<Page<RecommendationResponse>> getRecommendations(
        @RequestParam Long userId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {
        Page<RecommendationResponse> recommendations = teacherService.getRecommendations(userId, page, size);
        return ApiResponse.success(recommendations);
    }

    /**
     * 获取热门推荐
     */
    @GetMapping("/recommendations/trending")
    public ApiResponse<List<RecommendationResponse>> getTrendingRecommendations(
        @RequestParam Long userId) {
        List<RecommendationResponse> recommendations = teacherService.getTrendingRecommendations(userId);
        return ApiResponse.success(recommendations);
    }

    /**
     * 获取相关推荐
     */
    @GetMapping("/recommendations/related")
    public ApiResponse<List<RecommendationResponse>> getRelatedRecommendations(
        @RequestParam Long userId,
        @RequestParam Long bookId) {
        List<RecommendationResponse> recommendations = teacherService.getRelatedRecommendations(userId, bookId);
        return ApiResponse.success(recommendations);
    }

    /**
     * 申请续借
     */
    @PostMapping("/renew")
    public ApiResponse<String> requestRenewal(
        @RequestParam Long userId,
        @RequestParam Long recordId) {
        boolean success = teacherService.requestRenewal(userId, recordId, 14L);
        if (success) {
            return ApiResponse.success("续借申请已提交");
        }
        return ApiResponse.fail("续借申请失败");
    }

    /**
     * 查看续借申请状态
     */
    @GetMapping("/renew/status")
    public ApiResponse<List<BookRenewRequest>> getRenewalStatus(
        @RequestParam Long userId) {
        List<BookRenewRequest> requests = teacherService.getRenewalRequests(userId);
        return ApiResponse.success(requests);
    }
}
