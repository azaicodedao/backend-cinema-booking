package com.cinema.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO đại diện cho phản hồi REST chuẩn, bao gồm mã trạng thái, lỗi (nếu có),
 * thông báo và dữ liệu.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO đại diện cho phản hồi REST chuẩn, bao gồm mã trạng thái, lỗi (nếu có), thông báo và dữ liệu.
 */
public class RestResponse<T> {
    private int statusCode;
    private String error;
    private String message;
    private T data;

    public static <T> RestResponse<T> success(T data) {
        return RestResponse.<T>builder()
                .statusCode(200)
                .error(null)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> RestResponse<T> success(T data, String message) {
        return RestResponse.<T>builder()
                .statusCode(200)
                .error(null)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> RestResponse<T> error(int statusCode, String error, String message) {
        return RestResponse.<T>builder()
                .statusCode(statusCode)
                .error(error)
                .message(message)
                .data(null)
                .build();
    }
}