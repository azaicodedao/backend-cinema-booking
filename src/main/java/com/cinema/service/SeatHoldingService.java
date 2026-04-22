package com.cinema.service;

import com.cinema.dto.SeatStatusMessageDto;
import com.cinema.entity.*;
import com.cinema.repository.*;
import com.cinema.entity.User;
import com.cinema.repository.UserRepository;
import com.cinema.enums.BookingStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatHoldingService {

    SeatHoldingRepository seatHoldingRepository;
    SeatRepository seatRepository;
    ShowtimeRepository showtimeRepository;
    TicketRepository ticketRepository;
    BookingRepository bookingRepository;
    UserRepository userRepository;
    SimpMessagingTemplate messagingTemplate;

    static final int HOLD_DURATION_MINUTES = 10;

    @Transactional
    public SeatStatusMessageDto holdSeat(Integer seatId, Integer showtimeId, Integer userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found"));

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found"));

        if (!seat.getRoom().getId().equals(showtime.getRoom().getId())) {
            throw new IllegalArgumentException("Seat does not belong to the showtime's room");
        }

        if (isSeatBookedForShowtime(seatId, showtimeId)) {
            throw new IllegalArgumentException("Seat is already booked for this showtime");
        }

        Optional<SeatHolding> existingHold = seatHoldingRepository.findBySeatIdAndShowtimeId(seatId, showtimeId);
        if (existingHold.isPresent()) {
            SeatHolding hold = existingHold.get();
            if (hold.getExpiredAt().isAfter(LocalDateTime.now())) {
                if (hold.getUser().getId().equals(userId)) {
                    hold.setExpiredAt(LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES));
                    seatHoldingRepository.save(hold);
                } else {
                    throw new IllegalArgumentException("Seat is already held by another user");
                }
            } else {
                hold.setUser(userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found")));
                hold.setExpiredAt(LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES));
                seatHoldingRepository.save(hold);
            }
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            SeatHolding seatHolding = SeatHolding.builder()
                    .seat(seat)
                    .showtime(showtime)
                    .user(user)
                    .expiredAt(LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES))
                    .build();
            seatHoldingRepository.save(seatHolding);
        }

        SeatStatusMessageDto message = SeatStatusMessageDto.builder()
                .seatId(seatId)
                .showtimeId(showtimeId)
                .status("HOLDING")
                .holdByUserId(userId)
                .expiredAt(LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES))
                .rowLetter(seat.getRowLabel())
                .seatNumber(seat.getColNumber())
                .build();

        messagingTemplate.convertAndSend("/topic/showtime/" + showtimeId, message);

        return message;
    }

    @Transactional
    public SeatStatusMessageDto releaseSeat(Integer seatId, Integer showtimeId, Integer userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found"));

        Optional<SeatHolding> existingHold = seatHoldingRepository.findBySeatIdAndShowtimeId(seatId, showtimeId);
        if (existingHold.isPresent()) {
            SeatHolding hold = existingHold.get();
            if (!hold.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("You can only release your own held seats");
            }
            seatHoldingRepository.delete(hold);
        }

        SeatStatusMessageDto message = SeatStatusMessageDto.builder()
                .seatId(seatId)
                .showtimeId(showtimeId)
                .status("AVAILABLE")
                .holdByUserId(null)
                .expiredAt(null)
                .rowLetter(seat.getRowLabel())
                .seatNumber(seat.getColNumber())
                .build();

        messagingTemplate.convertAndSend("/topic/showtime/" + showtimeId, message);

        return message;
    }

    /**
     * Giữ nhiều ghế cùng một lúc. Thường dùng khi người dùng chọn nhiều ghế trên UI.
     */
    @Transactional
    public List<SeatStatusMessageDto> holdSeats(List<Integer> seatIds, Integer showtimeId, Integer userId) {
        List<SeatStatusMessageDto> results = new ArrayList<>();
        for (Integer seatId : seatIds) {
            results.add(holdSeat(seatId, showtimeId, userId));
        }
        return results;
    }

    /**
     * Giải phóng nhiều ghế cùng một lúc.
     */
    @Transactional
    public List<SeatStatusMessageDto> releaseSeats(List<Integer> seatIds, Integer showtimeId, Integer userId) {
        List<SeatStatusMessageDto> results = new ArrayList<>();
        for (Integer seatId : seatIds) {
            results.add(releaseSeat(seatId, showtimeId, userId));
        }
        return results;
    }

    /**
     * Lấy trạng thái tất cả các ghế cho một Suất chiếu.
     *
     * === Phiên bản đã tối ưu hóa (Bulk Fetch) ===
     * Thay vì lặp từng ghế và chạy SQL riêng cho mỗi ghế (gây lỗi N+1),
     * phiên bản này tải toàn bộ dữ liệu cần thiết vào bộ nhớ trước,
     * sau đó dùng HashMap để tra cứu trạng thái với độ phức tạp O(1).
     *
     * Tổng số truy vấn SQL: cố định 4 câu (bất kể rạp có bao nhiêu ghế).
     *
     * Logic ưu tiên:
     * 1. Ghế đã thanh toán (CONFIRMED) → BOOKED
     * 2. Ghế đang trong đơn hàng chờ thanh toán (PENDING) → HOLDING (có userId)
     * 3. Ghế đang giữ tạm thời (seat_holdings) → HOLDING (có userId)
     * 4. Còn lại → AVAILABLE
     */
    @Transactional(readOnly = true)
    public List<SeatStatusMessageDto> getSeatsStatusForShowtime(Integer showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found"));

        // === QUERY 1: Lấy toàn bộ ghế của phòng chiếu ===
        List<Seat> seats = seatRepository.findByRoomId(showtime.getRoom().getId());

        // === QUERY 2: Lấy toàn bộ đơn hàng (chưa hủy) của suất chiếu ===
        List<Booking> bookings = bookingRepository.findByShowtimeId(showtimeId);
        List<Booking> activeBookings = bookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .toList();

        // === QUERY 3: Lấy toàn bộ vé của các đơn hàng đó (1 câu SQL duy nhất) ===
        // Đây là điểm tối ưu cốt lõi: thay vì N lần findByBooking, chỉ cần 1 lần findByBookingIn
        List<Ticket> allTickets = activeBookings.isEmpty()
                ? List.of()
                : ticketRepository.findByBookingIn(activeBookings);

        // Xây dựng bảng tra cứu: seatId → Booking (để biết ghế thuộc đơn hàng nào)
        Map<Integer, Booking> seatIdToBooking = new HashMap<>();
        for (Ticket ticket : allTickets) {
            seatIdToBooking.put(ticket.getSeat().getId(), ticket.getBooking());
        }

        // === QUERY 4: Lấy toàn bộ lượt giữ chỗ tạm thời còn hiệu lực ===
        List<SeatHolding> activeHolds = seatHoldingRepository
                .findByShowtimeIdAndExpiredAtAfter(showtimeId, LocalDateTime.now());

        // Xây dựng bảng tra cứu: seatId → SeatHolding
        Map<Integer, SeatHolding> seatIdToHold = new HashMap<>();
        for (SeatHolding hold : activeHolds) {
            seatIdToHold.put(hold.getSeat().getId(), hold);
        }

        // === Duyệt qua từng ghế và gán trạng thái (chỉ dùng Map, không query DB) ===
        List<SeatStatusMessageDto> result = new ArrayList<>();
        for (Seat seat : seats) {
            SeatStatusMessageDto.SeatStatusMessageDtoBuilder builder = SeatStatusMessageDto.builder()
                    .seatId(seat.getId())
                    .showtimeId(showtimeId)
                    .rowLetter(seat.getRowLabel())
                    .seatNumber(seat.getColNumber());

            // Tra cứu đơn hàng chứa ghế này (O(1) thay vì O(M*SQL))
            Booking booking = seatIdToBooking.get(seat.getId());

            if (booking != null) {
                if (booking.getStatus() == BookingStatus.CONFIRMED) {
                    builder.status("BOOKED");
                } else {
                    // Trạng thái PENDING → Hiển thị là HOLDING để người dùng thấy ghế của mình
                    builder.status("HOLDING")
                           .holdByUserId(booking.getUser().getId())
                           .expiredAt(booking.getCreatedAt().plusMinutes(10));
                }
            } else {
                // Không thuộc đơn hàng nào → kiểm tra giữ chỗ tạm thời
                SeatHolding hold = seatIdToHold.get(seat.getId());
                if (hold != null) {
                    builder.status("HOLDING")
                            .holdByUserId(hold.getUser().getId())
                            .expiredAt(hold.getExpiredAt());
                } else {
                    builder.status("AVAILABLE");
                }
            }

            result.add(builder.build());
        }

        return result;
    }

    @Transactional
    public void releaseExpiredHolds() {
        List<SeatHolding> expiredHolds = seatHoldingRepository.findByExpiredAtBefore(LocalDateTime.now());

        if (expiredHolds.isEmpty()) return;

        var holdsByShowtime = expiredHolds.stream()
                .collect(Collectors.groupingBy(h -> h.getShowtime().getId()));

        seatHoldingRepository.deleteAll(expiredHolds);

        for (var entry : holdsByShowtime.entrySet()) {
            Integer showtimeId = entry.getKey();
            for (SeatHolding hold : entry.getValue()) {
                SeatStatusMessageDto message = SeatStatusMessageDto.builder()
                        .seatId(hold.getSeat().getId())
                        .showtimeId(showtimeId)
                        .status("AVAILABLE")
                        .holdByUserId(null)
                        .expiredAt(null)
                        .rowLetter(hold.getSeat().getRowLabel())
                        .seatNumber(hold.getSeat().getColNumber())
                        .build();
                messagingTemplate.convertAndSend("/topic/showtime/" + showtimeId, message);
            }
        }
    }

    /**
     * Kiểm tra xem người dùng có đang giữ (hold) tất cả các ghế theo yêu cầu hay không.
     * Kiểm tra cả trong bảng SeatHolding và trong các đơn hàng PENDING của người dùng.
     */
    public boolean areSeatsHeldByUser(List<Integer> seatIds, Integer showtimeId, Integer userId) {
        // 1. Kiểm tra trong các bảng SeatHolding (giữ tạm thời)
        List<SeatHolding> userHolds = seatHoldingRepository.findByUserIdAndShowtimeId(userId, showtimeId);
        Set<Integer> heldSeatIds = userHolds.stream()
                .filter(h -> h.getExpiredAt().isAfter(LocalDateTime.now()))
                .map(h -> h.getSeat().getId())
                .collect(Collectors.toSet());

        // 2. Kiểm tra trong các đơn hàng PENDING của người dùng này cho cùng suất chiếu
        List<Booking> userPendingBookings = bookingRepository.findByShowtimeId(showtimeId).stream()
                .filter(b -> b.getUser().getId().equals(userId) && b.getStatus() == BookingStatus.PENDING)
                .toList();

        // Bulk fetch vé của các đơn hàng PENDING (tránh lỗi N+1)
        if (!userPendingBookings.isEmpty()) {
            List<Ticket> pendingTickets = ticketRepository.findByBookingIn(userPendingBookings);
            heldSeatIds.addAll(pendingTickets.stream()
                    .map(t -> t.getSeat().getId())
                    .toList());
        }

        return heldSeatIds.containsAll(seatIds);
    }

    @Transactional
    public void convertHoldsToBooked(List<Integer> seatIds, Integer showtimeId) {
        for (Integer seatId : seatIds) {
            seatHoldingRepository.findBySeatIdAndShowtimeId(seatId, showtimeId)
                    .ifPresent(seatHoldingRepository::delete);
            // Không gửi WebSocket ở đây vì trạng thái này sẽ do BookingService quyết định 
            // (có thể là HOLDING nếu chờ thanh toán hoặc BOOKED nếu đã trả tiền).
        }
    }

    private boolean isSeatBookedForShowtime(Integer seatId, Integer showtimeId) {
        return getBookedSeatIdsForShowtime(showtimeId).contains(seatId);
    }

    /**
     * Lấy tập hợp ID các ghế đã được đặt (CONFIRMED/PENDING) của một suất chiếu.
     * Đã tối ưu: dùng findByBookingIn thay vì lặp từng đơn hàng.
     */
    private Set<Integer> getBookedSeatIdsForShowtime(Integer showtimeId) {
        List<Booking> bookings = bookingRepository.findByShowtimeId(showtimeId);
        List<Booking> activeBookings = bookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .toList();
        if (activeBookings.isEmpty()) return Set.of();

        return ticketRepository.findByBookingIn(activeBookings).stream()
                .map(t -> t.getSeat().getId())
                .collect(Collectors.toSet());
    }
}
