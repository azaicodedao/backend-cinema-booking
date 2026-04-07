package com.cinema.repository;

import com.cinema.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Integer> {

    List<Showtime> findByMovieId(Integer movieId);

    List<Showtime> findByShowDateIsNull();

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
}
