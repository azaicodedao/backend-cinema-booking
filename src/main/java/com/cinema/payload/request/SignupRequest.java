package com.cinema.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    @NotBlank
    @Email
    @Size(min = 8, message = "Mật khẩu tối thiểu 8 ký tự")
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String fullName;
}

