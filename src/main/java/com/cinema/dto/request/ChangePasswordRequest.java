package com.cinema.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonAlias;

@Getter
@Setter
/**
 * DTO chứa yêu cầu thay đổi mật khẩu của người dùng, bao gồm mật khẩu cũ và mật khẩu mới để xác nhận.
 */
public class ChangePasswordRequest {
    @JsonAlias({"currentPassword", "old_password", "oldPassword"})
    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @JsonAlias({"password", "new_password", "newPassword"})
    @NotBlank(message = "New password is required")
    private String newPassword;

    @JsonAlias({"confirmPassword", "confirm_password", "confirmNewPassword"})
    private String confirmNewPassword;
}
