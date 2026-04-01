package com.cinema.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
/**
 * DTO đại diện cho một thông báo đơn giản trả về từ API.
 */
public class MessageResponse {
    private String message;
}
