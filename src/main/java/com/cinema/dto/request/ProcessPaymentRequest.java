package com.cinema.dto.request;

import lombok.Data;

@Data
/**
 * DTO chứa yêu cầu xử lý thanh toán cho một mã đặt vé, bao gồm phương thức và số tiền.
 */
public class ProcessPaymentRequest {
    private Integer bookingId;
    private String method; // CASH, MOMO, VNPAY
    private Double amount;
}
