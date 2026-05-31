package com.cinema.service;

import com.cinema.dto.BookingDetailDTO;
import com.cinema.dto.BookingResponseDTO;
import com.cinema.dto.request.BookingRequestDto;
import com.cinema.entity.*;
import com.cinema.repository.*;
import com.cinema.enums.TicketStatus;
import com.cinema.enums.BookingStatus;
import com.cinema.dto.SeatStatusMessageDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.extern.slf4j.Slf4j;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Dịch vụ xử lý các nghiệp vụ liên quan đến Đặt vé (Booking).
 * Quản lý luồng tạo đơn hàng, thanh toán và thông báo trạng thái ghế qua
 * WebSocket.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
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
    ShowtimeAvailabilityService showtimeAvailabilityService;

    /**
     * Tạo đơn đặt vé mới.
     * Kiểm tra trạng thái giữ chỗ của các ghế trước khi tạo Booking và Ticket.
     */
    @Transactional
    public Booking createBooking(BookingRequestDto request, Integer userId) {
        // Kiểm tra dữ liệu đầu vào
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found"));
        showtimeAvailabilityService.validateBookable(showtime);

        List<Integer> uniqueSeatIds = request.getSeatIds().stream().distinct().toList();
        List<Seat> seats = seatRepository.findAllById(uniqueSeatIds);

        if (seats.stream().anyMatch(s -> !s.getRoom().getId().equals(showtime.getRoom().getId()))) {
            throw new IllegalArgumentException("Seats do not belong to the correct room.");
        }

        // Kiểm tra quyền giữ ghế (hold tạm thời hoặc thuộc booking PENDING cũ)
        if (!seatHoldingService.areSeatsHeldByUser(uniqueSeatIds, request.getShowtimeId(), userId)) {
            throw new IllegalArgumentException(
                    "You must hold all selected seats before booking. Please select seats first.");
        }

        // Lấy phụ phí phòng và giá cơ bản (null-safe)
        BigDecimal roomSurcharge = (showtime.getRoom() != null && showtime.getRoom().getRoomType() != null)
                ? showtime.getRoom().getRoomType().getSurcharge()
                : BigDecimal.ZERO;
        if (roomSurcharge == null)
            roomSurcharge = BigDecimal.ZERO;
        BigDecimal basePrice = showtime.getBasePrice() != null ? showtime.getBasePrice() : BigDecimal.ZERO;

        BigDecimal basePriceWithRoom = basePrice.add(roomSurcharge);

        // Kiểm tra nếu sửa ghế thì vẫn dùng booking_id cũ, không tạo booking mới
        Booking existingPending = bookingRepository.findByShowtimeId(request.getShowtimeId()).stream()
                .filter(b -> b.getUser().getId().equals(userId) && b.getStatus() == BookingStatus.PENDING)
                .findFirst()
                .orElse(null);

        Booking booking;
        if (existingPending != null) {
            booking = handleUpdatePendingBooking(existingPending, seats, uniqueSeatIds, basePriceWithRoom,
                    request.getShowtimeId());
        } else {
            booking = handleCreateNewBooking(user, showtime, seats, basePriceWithRoom);
        }

        // Xóa các bản ghi SeatHolding tạm thời (ghế giờ thuộc booking)
        seatHoldingService.convertHoldsToBooked(uniqueSeatIds, request.getShowtimeId());

        // Gửi WS HOLDING cho tất cả ghế trong danh sách cuối cùng
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

        return booking;
    }

    /**
     * Xử lý thanh toán và hoàn tất đơn hàng.
     */
    @Transactional
    public void payBooking(Integer bookingId, String methodString) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        showtimeAvailabilityService.validateBookable(booking.getShowtime());

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

    /**
     * Lấy thông tin tóm tắt của đơn đặt (Trang lịch sử đặt)
     */
    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingSummary(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        List<Ticket> tickets = ticketRepository.findByBooking(booking);
        List<String> seatLabels = tickets.stream()
                .map(t -> t.getSeat().getRowLabel() + t.getSeat().getColNumber())
                .toList();

        long elapsedSeconds = java.time.Duration.between(booking.getCreatedAt(), java.time.LocalDateTime.now())
                .getSeconds();
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

    /**
     * Lấy thông tin chi tiết của đơn đặt (Trang chi tiết hóa đơn)
     * 
     */
    @Transactional(readOnly = true)
    public BookingDetailDTO getBookingDetail(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        List<Ticket> tickets = ticketRepository.findByBooking(booking);
        List<String> seatLabels = tickets.stream()
                .map(t -> t.getSeat().getRowLabel() + t.getSeat().getColNumber())
                .toList();

        List<BookingDetailDTO.TicketInfo> ticketInfos = new ArrayList<>();
        if (tickets != null) {
            for (Ticket t : tickets) {
                String label = "Unknown";
                String type = "Standard";
                if (t.getSeat() != null) {
                    label = (t.getSeat().getRowLabel() != null ? t.getSeat().getRowLabel() : "")
                            + (t.getSeat().getColNumber() != null ? t.getSeat().getColNumber() : "");
                    if (t.getSeat().getSeatType() != null) {
                        type = t.getSeat().getSeatType().getName();
                    }
                }
                ticketInfos.add(BookingDetailDTO.TicketInfo.builder()
                        .ticketId(t.getId())
                        .seatLabel(label)
                        .seatType(type)
                        .qrCode(t.getQrCode())
                        .build());
            }
        }

        long elapsedSeconds = java.time.Duration.between(booking.getCreatedAt(), java.time.LocalDateTime.now())
                .getSeconds();
        int remainingSeconds = Math.max(0, 600 - (int) elapsedSeconds);

        // --- Truy vấn bảng Payments để lấy thông tin biên lai thanh toán ---
        java.util.Optional<Payment> paymentOpt = paymentRepository.findByBookingId(bookingId);

        BookingDetailDTO.BookingDetailDTOBuilder builder = BookingDetailDTO.builder()
                .bookingId(booking.getId())
                .movieId(booking.getShowtime().getMovie().getId())
                .showtimeId(booking.getShowtime().getId())
                .bookingCode(booking.getBookingCode())
                .movieTitle(booking.getShowtime().getMovie().getTitle())
                .posterUrl(booking.getShowtime().getMovie().getPosterUrl())
                .roomName(booking.getShowtime().getRoom().getName())
                .roomType(booking.getShowtime().getRoom().getRoomType().getName())
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

    /**
     * Lấy danh sách tất cả các đơn đặt của người dùng
     * 
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getUserBookings(Integer userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);

        if (bookings.isEmpty()) {
            return new ArrayList<>();
        }

        // Bulk fetch tickets
        List<Ticket> allTickets = ticketRepository.findByBookingIn(bookings);
        Map<Integer, List<Ticket>> ticketsByBookingId = allTickets.stream()
                .collect(Collectors.groupingBy(t -> t.getBooking().getId()));

        // Bulk fetch reviews
        List<Integer> bookingIds = bookings.stream().map(Booking::getId).toList();
        Set<Integer> reviewedBookingIds = reviewRepository.findByBookingIdIn(bookingIds).stream()
                .map(r -> r.getBooking().getId())
                .collect(Collectors.toSet());

        List<BookingResponseDTO> result = new ArrayList<>();
        for (Booking booking : bookings) {
            try {
                List<Ticket> tickets = ticketsByBookingId.getOrDefault(booking.getId(), new ArrayList<>());
                List<String> seatLabels = new ArrayList<>();
                if (tickets != null) {
                    for (Ticket t : tickets) {
                        if (t.getSeat() != null) {
                            String label = (t.getSeat().getRowLabel() != null ? t.getSeat().getRowLabel() : "")
                                    + (t.getSeat().getColNumber() != null ? t.getSeat().getColNumber() : "");
                            if (!label.isEmpty())
                                seatLabels.add(label);
                        }
                    }
                }

                // Null-safe check for times
                LocalDateTime createdAt = booking.getCreatedAt() != null ? booking.getCreatedAt() : LocalDateTime.now();
                long elapsedSeconds = java.time.Duration.between(createdAt, java.time.LocalDateTime.now()).getSeconds();
                int remainingSeconds = Math.max(0, 600 - (int) elapsedSeconds);

                // Null-safe check for associations
                String movieTitle = "Unknown Movie";
                Integer movieId = null;
                String roomName = "Unknown Room";
                LocalDateTime startTime = null;

                if (booking.getShowtime() != null) {
                    startTime = booking.getShowtime().getStartTime();
                    if (booking.getShowtime().getMovie() != null) {
                        movieTitle = booking.getShowtime().getMovie().getTitle();
                        movieId = booking.getShowtime().getMovie().getId();
                    }
                    if (booking.getShowtime().getRoom() != null) {
                        roomName = booking.getShowtime().getRoom().getName();
                    }
                }

                result.add(BookingResponseDTO.builder()
                        .bookingId(booking.getId())
                        .movieId(movieId)
                        .bookingCode(booking.getBookingCode())
                        .movieTitle(movieTitle)
                        .roomName(roomName)
                        .showtimeStart(startTime)
                        .seatLabels(seatLabels)
                        .totalPrice(booking.getTotalPrice() != null ? booking.getTotalPrice().doubleValue() : 0.0)
                        .status(booking.getStatus() != null ? booking.getStatus().name() : "UNKNOWN")
                        .createdAt(createdAt)
                        .paymentCountdownSeconds(remainingSeconds)
                        .numberOfTickets(tickets != null ? tickets.size() : 0)
                        .hasReviewed(reviewedBookingIds.contains(booking.getId()))
                        .build());
            } catch (Exception e) {
                // Log and skip this specific corrupt booking instead of crashing the whole list
                log.error("Error mapping booking ID {}: {}", booking.getId(), e.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * Tự động quét và Hủy các đơn hàng đang PENDING nhưng vượt quá 10 phút.
     * Cập nhật trạng thái thành CANCELLED và gửi thông báo Socket để nhả ghế cho
     * người khác.
     */
    @Transactional
    public void cancelExpiredPendingBookings() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        List<Booking> expiredBookings = bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING,
                tenMinutesAgo);

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

    private BigDecimal calculateTicketPrice(BigDecimal basePriceWithRoom, Seat seat) {
        BigDecimal seatSurcharge = (seat.getSeatType() != null)
                ? seat.getSeatType().getSurcharge()
                : BigDecimal.ZERO;
        if (seatSurcharge == null)
            seatSurcharge = BigDecimal.ZERO;

        return basePriceWithRoom.add(seatSurcharge);
    }

    /**
     * Xử lý cập nhật đơn hàng đang chờ (PENDING) khi người dùng thêm hoặc xóa ghế
     * trong thời gian đếm ngược.
     * 
     * @param booking           The existing pending booking to update
     * @param seats             The current list of Seat entities (including new and
     *                          existing)
     * @param uniqueSeatIds     The list of unique seat IDs selected by the user
     * @param basePriceWithRoom The base price per seat for this showtime (including
     *                          room surcharge)
     * @param showtimeId        The ID of the showtime
     * @return
     */
    private Booking handleUpdatePendingBooking(Booking booking, List<Seat> seats, List<Integer> uniqueSeatIds,
            BigDecimal basePriceWithRoom, Integer showtimeId) {
        List<Ticket> currentTickets = ticketRepository.findByBooking(booking);
        Set<Integer> newSeatIdSet = new HashSet<>(uniqueSeatIds);
        Set<Integer> currentSeatIds = currentTickets.stream()
                .map(t -> t.getSeat().getId())
                .collect(Collectors.toSet());
        // Xóa (Quét các ghế đã bị bỏ chọn)
        for (Ticket t : currentTickets) {
            if (!newSeatIdSet.contains(t.getSeat().getId())) {
                ticketRepository.delete(t);
                messagingTemplate.convertAndSend("/topic/showtime/" + showtimeId,
                        SeatStatusMessageDto.builder()
                                .seatId(t.getSeat().getId())
                                .showtimeId(showtimeId)
                                .status("AVAILABLE")
                                .rowLetter(t.getSeat().getRowLabel())
                                .seatNumber(t.getSeat().getColNumber())
                                .build());
            }
        }
        // Thêm (Quét các ghế mới được thêm vào)
        for (Seat seat : seats) {
            if (!currentSeatIds.contains(seat.getId())) {
                BigDecimal ticketPrice = calculateTicketPrice(basePriceWithRoom, seat);
                Ticket ticket = new Ticket();
                ticket.setBooking(booking);
                ticket.setSeat(seat);
                ticket.setQrCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                ticket.setStatus(TicketStatus.VALID);
                ticket.setPrice(ticketPrice);
                ticketRepository.save(ticket);
            }
        }
        // Tính toán lại Tổng tiền và Gia hạn thời gian
        List<Ticket> finalTickets = ticketRepository.findByBooking(booking);
        BigDecimal totalPrice = finalTickets.stream()
                .map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        booking.setTotalPrice(totalPrice);
        booking.setCreatedAt(LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    /**
     * Khởi tạo một đơn đặt vé hoàn toàn mới
     * 
     * @param user
     * @param showtime
     * @param seats
     * @param basePriceWithRoom
     * @return
     */
    private Booking handleCreateNewBooking(User user, Showtime showtime, List<Seat> seats,
            BigDecimal basePriceWithRoom) {
        Booking booking = new Booking();
        booking.setBookingCode("BKG" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setStatus(BookingStatus.PENDING);
        // Sản xuất từng tấm Vé (Tickets) và cộng dồn Tổng tiền
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<Ticket> tickets = new ArrayList<>();

        for (Seat seat : seats) {
            BigDecimal ticketPrice = calculateTicketPrice(basePriceWithRoom, seat);
            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setSeat(seat);
            ticket.setQrCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            ticket.setStatus(TicketStatus.VALID);
            ticket.setPrice(ticketPrice);

            tickets.add(ticket);
            totalPrice = totalPrice.add(ticketPrice);
        }

        booking.setTotalPrice(totalPrice);
        Booking savedBooking = bookingRepository.save(booking);

        for (Ticket t : tickets) {
            t.setBooking(savedBooking);
            ticketRepository.save(t);
        }
        return savedBooking;
    }
}
