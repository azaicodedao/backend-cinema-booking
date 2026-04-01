package com.cinema.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO chứa thông tin phản hồi khi đăng nhập thành công, bao gồm Access Token, Refresh Token và thông tin cơ bản của người dùng.
 */
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private UserInfo user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Integer id;
        private String fullName;
        private String email;
        private String avatarUrl;
        private String role;
    }
}
