package com.cinema.service;

import com.cinema.enums.Role;
import com.cinema.entity.User;
import com.cinema.enums.UserStatus;
import com.cinema.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserService {

    UserRepository userRepository;
    AuditLogService auditLogService;

    public Page<User> getUsers(String keyword, Role role, UserStatus status, Pageable pageable) {
        return userRepository.searchUsers(keyword, role, status, pageable);
    }

    public User getUserDetail(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public void changeRole(Integer adminId, Integer targetUserId, Role newRole) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String oldRole = user.getRole().name();
        user.setRole(newRole);
        userRepository.save(user);

        auditLogService.logAction(
                "CHANGE_ROLE",
                adminId,
                targetUserId,
                oldRole,
                newRole.name(),
                "Changed user role"
        );
    }

    @Transactional
    public void lockUser(Integer adminId, Integer targetUserId) {
        changeUserStatus(adminId, targetUserId, UserStatus.BLOCKED, "LOCK_USER", "Locked user account");
    }

    @Transactional
    public void unlockUser(Integer adminId, Integer targetUserId) {
        changeUserStatus(adminId, targetUserId, UserStatus.ACTIVE, "UNLOCK_USER", "Unlocked user account");
    }

    private void changeUserStatus(Integer adminId, Integer targetUserId, UserStatus newStatus, String action, String description) {
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
                    description
            );
        }
    }
}
