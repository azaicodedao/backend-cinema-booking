package com.cinema.repository;

import com.cinema.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.cinema.enums.BookingStatus;
import java.time.LocalDateTime;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByUserId(Integer userId);

    List<Booking> findByShowtimeId(Integer showtimeId);

    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime dateTime);

    // Mới thêm
    boolean existsByShowtimeIdAndStatus(Integer showtimeId, com.cinema.enums.BookingStatus status);
}
