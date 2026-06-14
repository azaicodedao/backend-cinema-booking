package com.cinema.service;

import com.cinema.enums.Role;
import com.cinema.entity.User;
import com.cinema.enums.UserStatus;
import com.cinema.dto.response.AdminUserResponse;
import com.cinema.mapper.UserMapper;
import com.cinema.repository.UserRepository;
import com.cinema.repository.RefreshTokenRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cinema.dto.request.AdminChangePasswordRequest;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserService {

    UserRepository userRepository;
    AuditLogService auditLogService;
    RefreshTokenRepository refreshTokenRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;

    // Lấy danh sách người dùng
    public Page<AdminUserResponse> getUsers(String keyword, Role role, UserStatus status, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(keyword, role, status, pageable);
        return users.map(userMapper::toAdminResponse);
    }

    // Lấy thông tin chi tiết của một người dùng cụ thể.
    public AdminUserResponse getUserDetail(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userMapper.toAdminResponse(user);
    }

    // Thay đổi vai trò của người dùng
    @Transactional
    public void changeRole(Integer adminId, Integer targetUserId, Role newRole) {
        if (adminId.equals(targetUserId)) {
            throw new IllegalArgumentException("Bạn không thể tự thay đổi quyền của chính mình.");
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
            long activeAdminCount = userRepository.countByRoleAndStatus(Role.ADMIN, UserStatus.ACTIVE);
            if (activeAdminCount <= 1) {
                throw new IllegalArgumentException("Không thể hạ quyền của Admin duy nhất trong hệ thống.");
            }
        }

        String oldRole = user.getRole().name();
        user.setRole(newRole);
        userRepository.save(user);

        // Thu hồi token của người dùng bị đổi quyền để họ phải đăng nhập lại với quyền
        // mới
        refreshTokenRepository.deleteByUser_Id(targetUserId);

        auditLogService.logAction(
                "CHANGE_ROLE",
                adminId,
                targetUserId,
                oldRole,
                newRole.name(),
                "Changed user role");
    }

    // Khoá tài khoản người dùng
    @Transactional
    public void lockUser(Integer adminId, Integer targetUserId) {
        if (adminId.equals(targetUserId)) {
            throw new IllegalArgumentException("Không thể khoá tài khoản đang đăng nhập.");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (targetUser.getRole() == Role.ADMIN && targetUser.getStatus() == UserStatus.ACTIVE) {
            long activeAdminCount = userRepository.countByRoleAndStatus(Role.ADMIN, UserStatus.ACTIVE);
            if (activeAdminCount <= 1) {
                throw new IllegalArgumentException("Không thể khoá tài khoản Admin duy nhất.");
            }
        }

        changeUserStatus(adminId, targetUserId, UserStatus.BLOCKED, "LOCK_USER", "Locked user account");

        refreshTokenRepository.deleteByUser_Id(targetUserId);
    }

    // Mở khoá tài khoản người dùng
    @Transactional
    public void unlockUser(Integer adminId, Integer targetUserId) {
        changeUserStatus(adminId, targetUserId, UserStatus.ACTIVE, "UNLOCK_USER", "Unlocked user account");
    }

    // Thay đổi mật khẩu người dùng
    @Transactional
    public void changePassword(Integer adminId, Integer targetUserId, AdminChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Xoá tất cả Refresh Token để bắt buộc người dùng đăng nhập lại với mật khẩu
        // mới
        refreshTokenRepository.deleteByUser_Id(targetUserId);
        auditLogService.logAction(
                "CHANGE_PASSWORD",
                adminId,
                targetUserId,
                null,
                null,
                "Admin changed user password");
    }

    // Thay đổi trạng thái của người dùng
    private void changeUserStatus(Integer adminId, Integer targetUserId, UserStatus newStatus, String action,
            String description) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String oldStatus = user.getStatus().name();
        if (!oldStatus.equals(newStatus.name())) {
            user.setStatus(newStatus);
            userRepository.save(user);

            auditLogService.logAction(
                    action,
                    adminId,
                    targetUserId,
                    oldStatus,
                    newStatus.name(),
                    description);
        }
    }
}
