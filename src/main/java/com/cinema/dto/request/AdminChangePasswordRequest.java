package com.cinema.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * DTO chứa yêu cầu thay đổi mật khẩu của người dùng bởi Admin.
 */
public class AdminChangePasswordRequest {

    @NotBlank(message = "New password is required")
    private String newPassword;

    @NotBlank(message = "Confirm new password is required")
    private String confirmNewPassword;
}
