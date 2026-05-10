package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO (Data Transfer Object) chứa thông tin phản hồi sau khi yêu cầu đặt vé
 * thành công,
 * bao gồm mã đặt vé, thông tin hiển thị tóm tắt và thời gian đếm ngược thanh
 * toán.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO chứa thông tin phản hồi sau khi yêu cầu đặt vé thành công, bao gồm mã đặt
 * vé và thời gian đếm ngược thanh toán.
 */
public class BookingResponseDTO {
    // --- THÔNG TIN ĐỊNH DANH ---
    private Integer bookingId;       // Khóa chính: Dùng làm ID chuyển hướng URL (/pay/:id hoặc /tickets/:id) ở trang Profile.jsx
    private Integer movieId;         // Dữ liệu dự phòng: Dùng để trỏ về trang chi tiết phim nếu cần mở rộng UI
    private String bookingCode;      // Dữ liệu dự phòng: Có thể in thêm mã giao dịch nhỏ (#ABCD) vào thẻ lịch sử

    // --- THÔNG TIN HIỂN THỊ CƠ BẢN (Đang được Profile.jsx dùng) ---
    private String movieTitle;       // Hiển thị tên phim in đậm trên từng dòng lịch sử
    private String posterUrl;        // Dữ liệu mở rộng: Dùng nếu sau này muốn thêm ảnh bìa mini vào thẻ lịch sử
    private String roomName;         // Hiển thị tên phòng chiếu (VD: 2 vé · Phòng 1)
    private LocalDateTime showtimeStart; // Hiển thị ngày và giờ bắt đầu suất chiếu
    private List<String> seatLabels; // Hiển thị danh sách ghế (VD: Ghế A1, A2)
    private Integer numberOfTickets; // Hiển thị tổng số vé đã mua

    // --- THÔNG TIN THANH TOÁN & TRẠNG THÁI ---
    private Double totalPrice;       // Hiển thị tổng tiền của toàn bộ đơn hàng
    private String status;           // Rẽ nhánh UI: Hiện màu huy hiệu (CONFIRMED/PENDING/CANCELLED) và quyết định link chuyển trang
    private LocalDateTime createdAt; // Thời gian tạo đơn (Metadata đối soát/sắp xếp lịch sử)
    private Integer paymentCountdownSeconds; // Dữ liệu dự phòng cho việc đếm ngược (Profile UI hiện không dùng trực tiếp)
    private Boolean hasReviewed;     // Hiện huy hiệu "✓ Đã đánh giá" thay vì nút "Đánh giá phim" nếu đã review xong
}
