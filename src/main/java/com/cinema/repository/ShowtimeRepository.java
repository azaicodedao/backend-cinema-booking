package com.cinema.repository;

import com.cinema.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Integer> {

    List<Showtime> findByMovieId(Integer movieId);

    List<Showtime> findByShowDateIsNull();

    boolean existsByRoomId(Integer roomId);

    /**
     * Lấy tất cả suất chiếu của một ngày cụ thể, sắp xếp theo giờ bắt đầu tăng dần.
     * Dùng cho API Lịch chiếu (UC06) để nhóm theo phim phía service.
     */
    List<Showtime> findByShowDateOrderByStartTimeAsc(LocalDate showDate);

    /**
     * Đếm số ghế còn trống của một suất chiếu dựa trên tổng ghế của phòng
     * trừ đi số ghế đã được book (trạng thái CONFIRMED).
     */
    @Query("""
                SELECT (r.totalRows * r.totalCols) - COUNT(b.id)
                FROM Showtime s
                JOIN s.room r
                LEFT JOIN Booking b ON b.showtime.id = s.id AND b.status = 'CONFIRMED'
                WHERE s.id = :showtimeId
                GROUP BY r.totalRows, r.totalCols
            """)
    Integer countAvailableSeats(@Param("showtimeId") Integer showtimeId);

    boolean existsByMovieIdAndStartTimeAfter(Integer movieId, LocalDateTime now);

    // Mới thêm
    @Query("""
            SELECT COUNT(s) > 0 FROM Showtime s
            WHERE s.room.id = :roomId
            AND s.startTime < :endTime
            AND s.endTime > :startTime
            AND s.status != 'CLOSED'
            """)
    boolean hasConflict(
            @Param("roomId") Integer roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("""
            SELECT COUNT(s) > 0 FROM Showtime s
            WHERE s.room.id = :roomId
            AND s.id != :showtimeId
            AND s.startTime < :endTime
            AND s.endTime > :startTime
            AND s.status != 'CLOSED'
            """)
    boolean hasConflictExcludeId(
            @Param("roomId") Integer roomId,
            @Param("showtimeId") Integer showtimeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
