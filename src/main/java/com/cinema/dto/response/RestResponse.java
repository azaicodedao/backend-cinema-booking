package com.cinema.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestResponse<T> {
    private int statusCode;
    private String error;
    private Object message;
    private T data;

    public static <T> RestResponse<T> success(T data) {
        return RestResponse.<T>builder()
                .statusCode(200)
                .data(data)
                .build();
    }

    public static <T> RestResponse<T> success(T data, Object message) {
        return RestResponse.<T>builder()
                .statusCode(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> RestResponse<T> error(int statusCode, String error) {
        return RestResponse.<T>builder()
                .statusCode(statusCode)
                .error(error)
                .build();
    }

    public static <T> RestResponse<T> error(int statusCode, String error, Object message) {
        return RestResponse.<T>builder()
                .statusCode(statusCode)
                .error(error)
                .message(message)
                .build();
    }
}
