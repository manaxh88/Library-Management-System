package com.example.library.controller;

import com.example.library.dto.ApiResponse;
import com.example.library.dto.AdminRenewRequestResponse;
import com.example.library.dto.BorrowStatisticsResponse;
import com.example.library.dto.RecommendationConfigResponse;
import com.example.library.dto.UserStatisticsResponse;
import com.example.library.entity.OperationLog;
import com.example.library.entity.SysUser;
import com.example.library.service.AdminService;
import com.example.library.util.RequireRole;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping({"/api/admin", "/admin"})
@RequireRole(roles = {0})  // 仅管理员可访问
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public ApiResponse<Page<SysUser>> getAllUsers(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword) {
        Page<SysUser> users = adminService.getAllUsers(page, size, keyword);
        return ApiResponse.success(users);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/users/{userId}")
    public ApiResponse<SysUser> getUserDetail(@PathVariable Long userId) {
        return adminService.getUserDetail(userId)
            .map(ApiResponse::success)
            .orElseGet(() -> ApiResponse.fail("用户不存在"));
    }

    /**
     * 冻结用户
     */
    @PutMapping("/users/{userId}/freeze")
    public ApiResponse<String> freezeUser(@PathVariable Long userId,
        @RequestParam Long adminId) {
        boolean success = adminService.freezeUser(userId, adminId);
        if (success) {
            return ApiResponse.success("用户已冻结");
        }
        return ApiResponse.fail("冻结用户失败");
    }

    /**
     * 解冻用户
     */
    @PutMapping("/users/{userId}/unfreeze")
    public ApiResponse<String> unfreezeUser(@PathVariable Long userId,
        @RequestParam Long adminId) {
        boolean success = adminService.unfreezeUser(userId, adminId);
        if (success) {
            return ApiResponse.success("用户已解冻");
        }
        return ApiResponse.fail("解冻用户失败");
    }

    /**
     * 重置密码
     */
    @PutMapping("/users/{userId}/reset-password")
    public ApiResponse<String> resetPassword(@PathVariable Long userId,
        @RequestParam String newPassword,
        @RequestParam Long adminId) {
        if (newPassword == null || newPassword.length() < 6) {
            return ApiResponse.fail("密码长度至少6位");
        }
        boolean success = adminService.resetPassword(userId, newPassword, adminId);
        if (success) {
            return ApiResponse.success("密码已重置");
        }
        return ApiResponse.fail("重置密码失败");
    }

    /**
     * 修改用户角色
     */
    @PutMapping("/users/{userId}/role")
    public ApiResponse<String> changeUserRole(@PathVariable Long userId,
        @RequestParam Integer newRole,
        @RequestParam Long adminId) {
        boolean success = adminService.changeUserRole(userId, newRole, adminId);
        if (success) {
            return ApiResponse.success("用户角色已修改");
        }
        return ApiResponse.fail("修改用户角色失败");
    }

    /**
     * 获取借阅统计
     */
    @GetMapping("/statistics/borrow")
    public ApiResponse<BorrowStatisticsResponse> getBorrowStatistics() {
        BorrowStatisticsResponse stats = adminService.getBorrowStatistics();
        return ApiResponse.success(stats);
    }

    @GetMapping("/statistics/users")
    public ApiResponse<UserStatisticsResponse> getUserStatistics() {
        UserStatisticsResponse stats = adminService.getUserStatistics();
        return ApiResponse.success(stats);
    }

    /**
     * 获取推荐配置
     */
    @GetMapping("/config/recommendation")
    public ApiResponse<RecommendationConfigResponse> getRecommendationConfig() {
        return adminService.getRecommendationConfig()
            .map(ApiResponse::success)
            .orElseGet(() -> ApiResponse.fail("推荐配置不存在"));
    }

    /**
     * 更新推荐配置
     */
    @PutMapping("/config/recommendation")
    public ApiResponse<String> updateRecommendationConfig(
        @RequestBody RecommendationConfigResponse configRequest,
        @RequestParam Long adminId) {
        boolean success = adminService.updateRecommendationConfig(configRequest, adminId);
        if (success) {
            return ApiResponse.success("推荐配置已更新");
        }
        return ApiResponse.fail("更新推荐配置失败");
    }

    /**
     * 重算推荐结果（可指定用户）
     */
    @PostMapping("/recommendation/rebuild")
    public ApiResponse<String> rebuildRecommendations(
        @RequestParam Long adminId,
        @RequestParam(required = false) Long userId,
        @RequestParam(defaultValue = "20") Integer topN) {
        int generatedCount = adminService.rebuildRecommendations(userId, topN, adminId);
        return ApiResponse.success("推荐重算完成，生成条数：" + generatedCount);
    }

    /**
     * 获取操作日志
     */
    @GetMapping("/logs")
    public ApiResponse<Page<OperationLog>> getOperationLogs(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {
        Page<OperationLog> logs = adminService.getOperationLogs(page, size);
        return ApiResponse.success(logs);
    }

    @GetMapping("/renew/requests")
    public ApiResponse<List<AdminRenewRequestResponse>> getRenewRequests(
        @RequestParam(required = false) Integer status) {
        return ApiResponse.success(adminService.getRenewRequests(status));
    }

    @PostMapping("/renew/{renewId}/approve")
    public ApiResponse<String> approveRenewRequest(
        @PathVariable Long renewId,
        @RequestParam Long adminId) {
        boolean success = adminService.approveRenewRequest(renewId, adminId);
        if (success) {
            return ApiResponse.success("续借申请已批准");
        }
        return ApiResponse.fail("审批失败");
    }

    @PostMapping("/renew/{renewId}/reject")
    public ApiResponse<String> rejectRenewRequest(
        @PathVariable Long renewId,
        @RequestParam Long adminId,
        @RequestParam(required = false) String reason) {
        boolean success = adminService.rejectRenewRequest(renewId, adminId, reason);
        if (success) {
            return ApiResponse.success("续借申请已拒绝");
        }
        return ApiResponse.fail("拒绝失败");
    }
}
