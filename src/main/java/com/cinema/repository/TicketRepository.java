package com.cinema.repository;

import com.cinema.entity.Booking;
import com.cinema.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    Optional<Ticket> findByQrCode(String qrCode); // changed from ticketCode if mismatch

    List<Ticket> findByBooking(Booking booking);

    /**
     * Lấy tất cả vé thuộc nhiều đơn hàng cùng lúc (Bulk Fetch).
     * Sử dụng câu SQL: SELECT * FROM tickets WHERE booking_id IN (...)
     * Giúp tránh lỗi N+1 khi cần kiểm tra trạng thái ghế cho toàn bộ sơ đồ.
     *
     * @param bookings Danh sách đơn hàng cần lấy vé.
     * @return Tất cả vé thuộc các đơn hàng đó.
     */
    List<Ticket> findByBookingIn(Collection<Booking> bookings);
}
