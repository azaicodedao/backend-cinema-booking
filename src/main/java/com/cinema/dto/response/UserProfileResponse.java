package com.cinema.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin hồ sơ cá nhân cho người dùng đã đăng nhập.
 * Không bao gồm password, status hoặc các trường nội bộ nhạy cảm.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Integer id;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String gender;
    private String address;
    private String role;
    private LocalDateTime createdAt;
}
