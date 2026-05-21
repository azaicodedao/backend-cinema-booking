package com.cinema.dto;

import java.math.BigDecimal;

/**
 * DTO dùng để truyền dữ liệu biểu đồ doanh thu.
 * - label: Nhãn mốc thời gian hiển thị trên trục hoành (VD: "21/05", "T21", "Th5/26")
 * - revenue: Tổng doanh thu tương ứng với mốc thời gian đó
 */
public record ChartDataDto(String label, BigDecimal revenue) {
}
