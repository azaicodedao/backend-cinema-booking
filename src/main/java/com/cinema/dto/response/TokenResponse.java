package com.cinema.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TokenResponse {
    private String token;
    private String refreshToken;
    private Integer id;
    private String email;
    private String fullName;
    private List<String> roles;
}
