package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO chứa thông tin tóm tắt về vé đã mua, dùng cho việc hiển thị danh sách vé.
 */
public class TicketListDTO {
    private Integer ticketId;
    private String bookingCode;
    private String customerName;
    private String movieTitle;
    private String posterUrl;
    private String roomName;
    private String seatLabel;
    private String seatType;
    private LocalDateTime showtimeStart;
    private String qrCode;
    private String status; // BOOKED, USED
    private Double price;
}
