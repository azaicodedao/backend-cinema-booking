package com.cinema.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * DTO chứa yêu cầu thay đổi mật khẩu của người dùng, bao gồm mật khẩu cũ và mật khẩu mới để xác nhận.
 */
public class ChangePasswordRequest {
    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    private String newPassword;

    @NotBlank(message = "Confirm new password is required")
    private String confirmNewPassword;
}
