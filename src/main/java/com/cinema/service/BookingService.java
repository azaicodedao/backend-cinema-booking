package com.cinema.service;

import com.cinema.dto.BookingDetailDTO;
import com.cinema.dto.BookingResponseDTO;
 import com.cinema.dto.request.BookingRequestDto;
 import com.cinema.entity.*;
import com.cinema.repository.*;
import com.cinema.entity.User;
import com.cinema.repository.UserRepository;
import com.cinema.enums.TicketStatus;
import com.cinema.enums.BookingStatus;
import com.cinema.dto.SeatStatusMessageDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Dịch vụ xử lý các nghiệp vụ liên quan đến Đặt vé (Booking).
 * Quản lý luồng tạo đơn hàng, thanh toán và thông báo trạng thái ghế qua WebSocket.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {

    BookingRepository bookingRepository;
    ShowtimeRepository showtimeRepository;
    SeatRepository seatRepository;
    TicketRepository ticketRepository;
    UserRepository userRepository;
    SeatHoldingService seatHoldingService;
    ReviewRepository reviewRepository;
    PaymentRepository paymentRepository;
    SimpMessagingTemplate messagingTemplate; // Tiêm WebSocket template để gửi thông báo thời gian thực

    /**
     * Tạo đơn đặt vé mới.
     * Kiểm tra trạng thái giữ chỗ của các ghế trước khi tạo Booking và Ticket.
     */
    @Transactional
    public Booking createBooking(BookingRequestDto request, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found"));

        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());

        if (seats.stream().anyMatch(s -> !s.getRoom().getId().equals(showtime.getRoom().getId()))) {
            throw new IllegalArgumentException("Seats do not belong to the correct room.");
        }

        // --- Bước 1: Kiểm tra quyền giữ ghế (Bao gồm cả ghế từ đơn hàng PENDING cũ) ---
        // Gọi instance của service thay vì gọi tĩnh thông qua Class
        if (!seatHoldingService.areSeatsHeldByUser(request.getSeatIds(), request.getShowtimeId(), userId)) {
            throw new IllegalArgumentException("You must hold all selected seats before booking. Please select seats first.");
        }

        // --- Bước 2: Tự động hủy các đơn hàng PENDING cũ của người dùng này cho cùng suất chiếu ---
        // Điều này cho phép người dùng "quay lại và chọn lại ghế" mà không bị kẹt bởi đơn hàng cũ.
        // Thực hiện SAU KHI kiểm tra areSeatsHeldByUser để đảm bảo tính hợp lệ.
        List<Booking> oldPendingBookings = bookingRepository.findByShowtimeId(request.getShowtimeId()).stream()
                .filter(b -> b.getUser().getId().equals(userId) && b.getStatus() == BookingStatus.PENDING)
                .toList();
        for (Booking oldBooking : oldPendingBookings) {
            oldBooking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(oldBooking);

            // --- THÔNG BÁO WEBSOCKET: Ghế của đơn hàng bị hủy trở lại trạng thái TRỐNG ---
            List<Ticket> oldTickets = ticketRepository.findByBooking(oldBooking);
            for (Ticket t : oldTickets) {
                messagingTemplate.convertAndSend("/topic/showtime/" + request.getShowtimeId(), 
                    SeatStatusMessageDto.builder()
                        .seatId(t.getSeat().getId())
                        .showtimeId(request.getShowtimeId())
                        .status("AVAILABLE")
                        .rowLetter(t.getSeat().getRowLabel())
                        .seatNumber(t.getSeat().getColNumber())
                        .build());
            }
        }

        // Lấy phụ phí phòng (null-safe)
        BigDecimal roomSurcharge = (showtime.getRoom() != null && showtime.getRoom().getRoomType() != null)
                ? showtime.getRoom().getRoomType().getSurcharge()
                : BigDecimal.ZERO;
        if (roomSurcharge == null) roomSurcharge = BigDecimal.ZERO;

        BigDecimal basePrice = showtime.getBasePrice() != null ? showtime.getBasePrice() : BigDecimal.ZERO;

        Booking booking = new Booking();
        booking.setBookingCode("BKG" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setStatus(BookingStatus.PENDING);

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<Ticket> tickets = new ArrayList<>();

        for (Seat seat : seats) {
            // Lấy phụ phí ghế (null-safe)
            BigDecimal seatSurcharge = (seat.getSeatType() != null) 
                    ? seat.getSeatType().getSurcharge() 
                    : BigDecimal.ZERO;
            if (seatSurcharge == null) seatSurcharge = BigDecimal.ZERO;

            // Công thức: ticketPrice = basePrice + roomSurcharge + seatSurcharge
            BigDecimal ticketPrice = basePrice.add(roomSurcharge).add(seatSurcharge);

            Ticket ticket = new Ticket();
            ticket.setBooking(booking); // Chưa save booking nhưng quan hệ ManyToOne cần set object
            ticket.setSeat(seat);
            ticket.setQrCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            ticket.setStatus(TicketStatus.VALID); 
            ticket.setPrice(ticketPrice);
            
            tickets.add(ticket);
            totalPrice = totalPrice.add(ticketPrice);
        }

        booking.setTotalPrice(totalPrice);
        Booking savedBooking = bookingRepository.save(booking);

        // Lưu danh sách vé (có thể dùng @OneToMany cascade nếu đã cấu hình, ở đây lưu thủ công cho chắc)
        for (Ticket t : tickets) {
            t.setBooking(savedBooking);
            ticketRepository.save(t);
        }

        // Chuyển trạng thái giữ chỗ sang Đang giữ (PENDING booking)
        seatHoldingService.convertHoldsToBooked(request.getSeatIds(), request.getShowtimeId());

        // --- THÔNG BÁO WEBSOCKET: Ghế hiện tại chuyển sang trạng thái HOLDING (đang chờ thanh toán) ---
        for (Seat seat : seats) {
            messagingTemplate.convertAndSend("/topic/showtime/" + request.getShowtimeId(), 
                SeatStatusMessageDto.builder()
                    .seatId(seat.getId())
                    .showtimeId(request.getShowtimeId())
                    .status("HOLDING")
                    .holdByUserId(userId)
                    .rowLetter(seat.getRowLabel())
                    .seatNumber(seat.getColNumber())
                    .build());
        }

        return savedBooking;
    }

    @Transactional
    public void payBooking(Integer bookingId, String methodString) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
                
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("Booking already paid");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        com.cinema.enums.PaymentMethod enumMethod;
        try {
            enumMethod = com.cinema.enums.PaymentMethod.valueOf(methodString.toUpperCase());
        } catch (Exception e) {
            enumMethod = com.cinema.enums.PaymentMethod.VNPAY;
        }

        Payment payment = Payment.builder()
                .booking(booking)
                .method(enumMethod)
                .amount(booking.getTotalPrice())
                .status("SUCCESS")
                .transactionCode("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .idempotencyKey(UUID.randomUUID().toString())
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        List<Ticket> tickets = ticketRepository.findByBooking(booking);
        for (Ticket t : tickets) {
             t.setStatus(TicketStatus.VALID);
             ticketRepository.save(t);

             // --- THÔNG BÁO WEBSOCKET: Ghế chuyển sang trạng thái ĐÃ ĐẶT (BOOKED) ---
             messagingTemplate.convertAndSend("/topic/showtime/" + booking.getShowtime().getId(), 
                SeatStatusMessageDto.builder()
                    .seatId(t.getSeat().getId())
                    .showtimeId(booking.getShowtime().getId())
                    .status("BOOKED")
                    .rowLetter(t.getSeat().getRowLabel())
                    .seatNumber(t.getSeat().getColNumber())
                    .build());
         }
     }
 
    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingSummary(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        List<Ticket> tickets = ticketRepository.findByBooking(booking);
        List<String> seatLabels = tickets.stream()
                .map(t -> t.getSeat().getRowLabel() + t.getSeat().getColNumber())
                .toList();

        long elapsedSeconds = java.time.Duration.between(booking.getCreatedAt(), java.time.LocalDateTime.now()).getSeconds();
        int remainingSeconds = Math.max(0, 600 - (int) elapsedSeconds);

        return BookingResponseDTO.builder()
                .bookingId(booking.getId())
                .bookingCode(booking.getBookingCode())
                .movieTitle(booking.getShowtime().getMovie().getTitle())
                .roomName(booking.getShowtime().getRoom().getName())
                .showtimeStart(booking.getShowtime().getStartTime())
                .seatLabels(seatLabels)
                .totalPrice(booking.getTotalPrice().doubleValue())
                .status(booking.getStatus().name())
                .createdAt(booking.getCreatedAt())
                .paymentCountdownSeconds(remainingSeconds)
                .numberOfTickets(tickets.size())
                .build();
    }

    @Transactional(readOnly = true)
    public BookingDetailDTO getBookingDetail(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        List<Ticket> tickets = ticketRepository.findByBooking(booking);
        List<String> seatLabels = tickets.stream()
                .map(t -> t.getSeat().getRowLabel() + t.getSeat().getColNumber())
                .toList();

        List<BookingDetailDTO.TicketInfo> ticketInfos = tickets.stream()
                .map(t -> BookingDetailDTO.TicketInfo.builder()
                        .ticketId(t.getId())
                        .seatLabel(t.getSeat().getRowLabel() + t.getSeat().getColNumber())
                        .seatType(t.getSeat().getSeatType().getName())
                        .qrCode(t.getQrCode())
                        .build())
                .toList();

        long elapsedSeconds = java.time.Duration.between(booking.getCreatedAt(), java.time.LocalDateTime.now()).getSeconds();
        int remainingSeconds = Math.max(0, 600 - (int) elapsedSeconds);

        // --- Truy vấn bảng Payments để lấy thông tin biên lai thanh toán ---
        java.util.Optional<Payment> paymentOpt = paymentRepository.findByBookingId(bookingId);

        BookingDetailDTO.BookingDetailDTOBuilder builder = BookingDetailDTO.builder()
                .bookingId(booking.getId())
                .movieId(booking.getShowtime().getMovie().getId())
                .bookingCode(booking.getBookingCode())
                .movieTitle(booking.getShowtime().getMovie().getTitle())
                .posterUrl(booking.getShowtime().getMovie().getPosterUrl())
                .roomName(booking.getShowtime().getRoom().getName())
                .showtimeStart(booking.getShowtime().getStartTime())
                .seatLabels(seatLabels)
                .numberOfTickets(tickets.size())
                .totalPrice(booking.getTotalPrice().doubleValue())
                .status(booking.getStatus().name())
                .createdAt(booking.getCreatedAt())
                .paymentCountdownSeconds(remainingSeconds)
                .tickets(ticketInfos)
                .customerName(booking.getUser().getFullName())
                .hasReviewed(reviewRepository.existsByBookingId(booking.getId()));

        // Nếu đã có bản ghi thanh toán → gán thông tin biên lai vào DTO
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            builder.paymentMethod(payment.getMethod().name())
                   .transactionCode(payment.getTransactionCode())
                   .paidAt(payment.getPaidAt());
        }

        return builder.build();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getUserBookings(Integer userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        
        return bookings.stream().map(booking -> {
            List<Ticket> tickets = ticketRepository.findByBooking(booking);
            List<String> seatLabels = tickets.stream()
                    .map(t -> t.getSeat().getRowLabel() + t.getSeat().getColNumber())
                    .toList();

            long elapsedSeconds = java.time.Duration.between(booking.getCreatedAt(), java.time.LocalDateTime.now()).getSeconds();
            int remainingSeconds = Math.max(0, 600 - (int) elapsedSeconds);

            return BookingResponseDTO.builder()
                    .bookingId(booking.getId())
                    .movieId(booking.getShowtime().getMovie().getId())
                    .bookingCode(booking.getBookingCode())
                    .movieTitle(booking.getShowtime().getMovie().getTitle())
                    .roomName(booking.getShowtime().getRoom().getName())
                    .showtimeStart(booking.getShowtime().getStartTime())
                    .seatLabels(seatLabels)
                    .totalPrice(booking.getTotalPrice().doubleValue())
                    .status(booking.getStatus().name())
                    .createdAt(booking.getCreatedAt())
                    .paymentCountdownSeconds(remainingSeconds)
                    .numberOfTickets(tickets.size())
                    .hasReviewed(reviewRepository.existsByBookingId(booking.getId()))
                    .build();
        }).toList();
    }

    /**
     * Tự động quét và Hủy các đơn hàng đang PENDING nhưng vượt quá 10 phút.
     * Cập nhật trạng thái thành CANCELLED và gửi thông báo Socket để nhả ghế cho người khác.
     */
    @Transactional
    public void cancelExpiredPendingBookings() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        List<Booking> expiredBookings = bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING, tenMinutesAgo);

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            List<Ticket> tickets = ticketRepository.findByBooking(booking);
            for (Ticket t : tickets) {
                // Nhả ghế
                messagingTemplate.convertAndSend("/topic/showtime/" + booking.getShowtime().getId(), 
                    SeatStatusMessageDto.builder()
                        .seatId(t.getSeat().getId())
                        .showtimeId(booking.getShowtime().getId())
                        .status("AVAILABLE")
                        .rowLetter(t.getSeat().getRowLabel())
                        .seatNumber(t.getSeat().getColNumber())
                        .build());
            }
        }
    }
}
