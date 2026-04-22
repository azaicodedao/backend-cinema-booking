package com.cinema.repository;

import com.cinema.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByUserId(Integer userId);

    List<Booking> findByShowtimeId(Integer showtimeId);

    // Mới thêm
    boolean existsByShowtimeIdAndStatus(Integer showtimeId, com.cinema.enums.BookingStatus status);
}
