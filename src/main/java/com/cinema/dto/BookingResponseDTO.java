package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO (Data Transfer Object) chứa thông tin phản hồi sau khi yêu cầu đặt vé thành công, 
 * bao gồm mã đặt vé, thông tin hiển thị tóm tắt và thời gian đếm ngược thanh toán.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO chứa thông tin phản hồi sau khi yêu cầu đặt vé thành công, bao gồm mã đặt vé và thời gian đếm ngược thanh toán.
 */
public class BookingResponseDTO {
    private Integer bookingId;
    private Integer movieId;
    private String bookingCode;
    private String movieTitle;
    private String posterUrl;
    private String roomName;
    private LocalDateTime showtimeStart;
    private List<String> seatLabels;
    private Double totalPrice;
    private String status; // PENDING, CONFIRMED, CANCELLED
    private LocalDateTime createdAt;
     private Integer paymentCountdownSeconds;
     private Integer numberOfTickets;
     private Boolean hasReviewed;
 }
