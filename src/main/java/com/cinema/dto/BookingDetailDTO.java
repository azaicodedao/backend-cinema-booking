package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Data Transfer Object (DTO) chứa thông tin chi tiết về một giao dịch đặt vé, bao gồm thông tin phim, phòng chiếu và danh sách vé.
 */
public class BookingDetailDTO {
    private Integer bookingId;
    private Integer movieId;
    private String bookingCode;
    private String movieTitle;
    private String posterUrl;
    private String roomName;
    private LocalDateTime showtimeStart;
    private List<String> seatLabels;
    private Integer numberOfTickets;
    private Double totalPrice;
    private String status;
    private LocalDateTime createdAt;
    private Boolean hasReviewed;
    private String customerName;
    private List<TicketInfo> tickets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketInfo {
        private Integer ticketId;
        private String seatLabel;
        private String seatType;
        private String qrCode;
    }
}
