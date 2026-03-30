package com.cinema.repository;

import com.cinema.entity.SeatHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatHoldingRepository extends JpaRepository<SeatHolding, Integer> {
    Optional<SeatHolding> findBySeatIdAndShowtimeId(Integer seatId, Integer showtimeId);

    List<SeatHolding> findByShowtimeIdAndExpiredAtAfter(Integer showtimeId, LocalDateTime now);

    List<SeatHolding> findByUserIdAndShowtimeId(Integer userId, Integer showtimeId);

    List<SeatHolding> findByExpiredAtBefore(LocalDateTime now);

    void deleteByExpiredAtBefore(LocalDateTime now);
}
