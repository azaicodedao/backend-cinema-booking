package com.cinema.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * DTO chứa yêu cầu cập nhật thông tin cá nhân của người dùng, bao gồm họ tên, số điện thoại, địa chỉ và ảnh đại diện.
 */
public class UpdateProfileRequest {
    private String fullName;
    private String phone;
    private String gender;
    private String address;
    private String avatarUrl;
}
