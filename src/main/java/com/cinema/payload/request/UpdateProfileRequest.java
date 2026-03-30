package com.cinema.payload.request;

import com.cinema.enums.Gender;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {
    private String fullName;
    private String phone;
    private String address;
    private Gender gender;
    private String avatarUrl;
}

