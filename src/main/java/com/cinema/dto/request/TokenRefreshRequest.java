package com.cinema.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 * DTO chứa yêu cầu làm mới mã xác thực (Access Token) bằng Refresh Token.
 */
public class TokenRefreshRequest {
    @NotBlank
    private String refreshToken;
}
