package com.example.library.service;

import com.example.library.dto.CreateUserRequest;
import com.example.library.dto.CreateUserResponse;
import com.example.library.dto.UserProfileResponse;
import com.example.library.entity.SysUser;
import com.example.library.repository.BorrowRecordRepository;
import com.example.library.repository.SysUserRepository;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final int ROLE_ADMIN = 0;
    private static final int ROLE_TEACHER = 1;
    private static final int ROLE_STUDENT = 2;
    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_BORROWING = 0;
    private static final int ADMIN_MAX_LIMIT = 30;
    private static final int TEACHER_MAX_LIMIT = 20;
    private static final int STUDENT_MAX_LIMIT = 10;

    private final SysUserRepository sysUserRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(SysUserRepository sysUserRepository, BorrowRecordRepository borrowRecordRepository) {
        this.sysUserRepository = sysUserRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Optional<CreateUserResponse> createUser(CreateUserRequest request) {
        if (sysUserRepository.existsByUsername(request.getUsername())) {
            return Optional.empty();
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole(request.getRole());
        user.setDeptName(request.getDeptName());
        user.setMaxLimit(resolveMaxLimit(request.getRole()));
        user.setStatus(STATUS_ACTIVE);

        SysUser saved = sysUserRepository.save(user);
        return Optional.of(new CreateUserResponse(saved.getUserId(), saved.getUsername(), saved.getRole()));
    }

    public Optional<UserProfileResponse> getUserProfile(Long userId) {
        Optional<SysUser> optionalUser = sysUserRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }

        SysUser user = optionalUser.get();
        long currentBorrowCount = borrowRecordRepository.countByUserIdAndStatus(userId, STATUS_BORROWING);
        long totalBorrowCount = borrowRecordRepository.countByUserId(userId);

        return Optional.of(new UserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                user.getRealName(),
                user.getRole(),
                user.getDeptName(),
                user.getMaxLimit(),
                user.getStatus(),
                currentBorrowCount,
                totalBorrowCount));
    }

    public Optional<UserProfileResponse> getUserProfileByUsername(String username) {
        Optional<SysUser> optionalUser = sysUserRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }

        SysUser user = optionalUser.get();
        long currentBorrowCount = borrowRecordRepository.countByUserIdAndStatus(user.getUserId(), STATUS_BORROWING);
        long totalBorrowCount = borrowRecordRepository.countByUserId(user.getUserId());

        return Optional.of(new UserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                user.getRealName(),
                user.getRole(),
                user.getDeptName(),
                user.getMaxLimit(),
                user.getStatus(),
                currentBorrowCount,
                totalBorrowCount));
    }

    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<SysUser> optionalUser = sysUserRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return false;
        }

        SysUser user = optionalUser.get();
        // Check if old password matches
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        // Update password with new encoded password
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserRepository.save(user);
        return true;
    }

    private int resolveMaxLimit(Integer role) {
        if (role == null) {
            return STUDENT_MAX_LIMIT;
        }
        if (role == ROLE_ADMIN) {
            return ADMIN_MAX_LIMIT;
        }
        if (role == ROLE_TEACHER) {
            return TEACHER_MAX_LIMIT;
        }
        return STUDENT_MAX_LIMIT;
    }
}
