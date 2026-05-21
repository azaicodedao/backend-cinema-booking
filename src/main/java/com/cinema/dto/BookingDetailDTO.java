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
 * Data Transfer Object (DTO) chứa thông tin chi tiết về một giao dịch đặt vé,
 * bao gồm thông tin phim, phòng chiếu và danh sách vé.
 */
public class BookingDetailDTO {
    // --- THÔNG TIN ĐỊNH DANH & PHIM ---
    private Integer bookingId;      // ID định danh đơn hàng trong Database
    private Integer movieId;        // Dùng ở trang Đánh giá (MovieReview.jsx) để gửi API đánh giá đúng phim
    private Integer showtimeId;     // ID của suất chiếu (dùng để quay lại chọn ghế)
    private String bookingCode;     // Hiển thị làm Mã đặt vé (VD: #ABCD123) ở TicketView.jsx
    private String movieTitle;      // Hiển thị tên phim ở các trang Pay, OrderSummary, TicketView, MovieReview
    private String posterUrl;       // Dùng hiển thị ảnh bìa phim cực kỳ quan trọng ở trang MovieReview.jsx

    // --- THÔNG TIN SUẤT CHIẾU & PHÒNG CHIẾU ---
    private String roomName;        // Hiển thị tên phòng chiếu (OrderSummary, TicketView)
    private String roomType;        // Hiển thị loại phòng (OrderSummary, TicketView)
    private LocalDateTime showtimeStart; // Hiển thị ngày giờ chiếu

    // --- THÔNG TIN GHẾ & GIÁ TIỀN ---
    private List<String> seatLabels; // Hiển thị danh sách ghế đã chọn (A1, A2...)
    private Integer numberOfTickets; // Dùng để tính toán đơn giá vé lẻ ở khối OrderSummary.jsx
    private Double totalPrice;       // Hiển thị tổng tiền cần thanh toán

    // --- TRẠNG THÁI & THỜI GIAN ---
    private String status;           // Dùng để bôi màu trạng thái (CONFIRMED, PENDING, CANCELLED)
    private LocalDateTime createdAt; // Thời gian tạo đơn hàng (Metadata đối soát, debug)
    private Integer paymentCountdownSeconds; // Dùng ở Pay.jsx làm đồng hồ đếm ngược 10 phút giữ ghế
    private Boolean hasReviewed;     // Cờ logic rẽ nhánh UI: Hiện nút "Viết đánh giá" hay "Xem đánh giá"

    // --- THÔNG TIN KHÁCH HÀNG & BIÊN LAI THANH TOÁN ---
    private String customerName;     // In tên khách hàng lên mặt vé điện tử (TicketView.jsx)
    private String paymentMethod;    // Hiển thị phương thức thanh toán trong Biên lai
    private String transactionCode;  // Mã giao dịch thanh toán từ VNPAY/MoMo
    private LocalDateTime paidAt;    // Thời gian hoàn tất thanh toán

    // --- VÉ ĐIỆN TỬ (E-TICKETS) ---
    private List<TicketInfo> tickets; // Dùng ở TicketView.jsx để vẽ mã QR Code và chi tiết vé cho nhân viên quét

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
