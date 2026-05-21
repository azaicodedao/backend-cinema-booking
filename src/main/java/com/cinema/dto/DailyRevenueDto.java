package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO này dùng để lưu trữ thông tin doanh thu theo ngày.
 */
public class DailyRevenueDto {
    private LocalDate date;
    private BigDecimal revenue;
}
