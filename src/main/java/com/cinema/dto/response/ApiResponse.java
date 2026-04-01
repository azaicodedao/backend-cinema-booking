package com.cinema.dto.response;

/**
 * Record đại diện cho phản hồi chuẩn từ API, bao gồm trạng thái thành công, thông báo và dữ liệu.
 */
public record ApiResponse<T>(boolean success, String message, T data) {
}
