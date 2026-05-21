package com.cinema.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin người dùng dành cho trang quản trị Admin.
 * Bao gồm role, status, lastLoginAt để Admin quản lý, nhưng KHÔNG chứa password.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private Integer id;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String gender;
    private String address;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
