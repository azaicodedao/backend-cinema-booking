package com.cinema.dto.response;

import java.util.Map;

/**
 * Record đại diện cho phản hồi lỗi từ API, bao gồm mã lỗi, thông báo và chi tiết lỗi (nếu có).
 */
public record ErrorResponse(String code, String message, Map<String, String> details) {
}
