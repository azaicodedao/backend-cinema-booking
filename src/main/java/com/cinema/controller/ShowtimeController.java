package com.cinema.controller;

import com.cinema.dto.MovieScheduleDto;
import com.cinema.dto.response.RestResponse;
import com.cinema.dto.ShowtimeDto;
import com.cinema.service.ShowtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/showtimes")
public class ShowtimeController {

    @Autowired
    private ShowtimeService showtimeService;

    @GetMapping
    public ResponseEntity<RestResponse<List<ShowtimeDto>>> getAllShowtimes() {
        List<ShowtimeDto> showtimes = showtimeService.getAllShowtimes();
        return ResponseEntity.ok(RestResponse.success(showtimes, "Fetched showtimes successfully"));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<RestResponse<List<ShowtimeDto>>> getShowtimesByMovie(@PathVariable Integer movieId) {
        List<ShowtimeDto> showtimes = showtimeService.getShowtimesByMovie(movieId);
        return ResponseEntity.ok(RestResponse.success(showtimes, "Fetched showtimes successfully"));
    }

    /**
     * API Lịch chiếu (UC06): Lấy danh sách phim kèm suất chiếu theo ngày.
     * Nếu không truyền date, mặc định lấy ngày hôm nay.
     *
     * @param date ngày cần xem lịch, format: yyyy-MM-dd. VD: 2026-04-01
     * @return Mảng MovieScheduleDto, mỗi phần tử là 1 phim với danh sách suất chiếu
     *         lồng sẵn
     */
    @GetMapping("/schedule")
    public ResponseEntity<RestResponse<List<MovieScheduleDto>>> getSchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<MovieScheduleDto> schedule = showtimeService.getScheduleByDate(date);
        String msg = "Fetched schedule for " + (date != null ? date : LocalDate.now());
        return ResponseEntity.ok(RestResponse.success(schedule, msg));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<ShowtimeDto>> createShowtime(@RequestBody ShowtimeDto showtimeDto) {
        try {
            ShowtimeDto createdShowtime = showtimeService.createShowtime(showtimeDto);
            return ResponseEntity.ok(RestResponse.success(createdShowtime, "Tạo suất chiếu thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Bad Request", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RestResponse.error(500, "Internal Server Error",
                            "An error occurred while creating showtime"));
        }
    }

    // Mới thêm
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<ShowtimeDto>> updateShowtime(@PathVariable Integer id,
            @RequestBody ShowtimeDto showtimeDto) {
        try {
            ShowtimeDto updatedShowtime = showtimeService.updateShowtime(id, showtimeDto);
            return ResponseEntity.ok(RestResponse.success(updatedShowtime, "Cập nhật suất chiếu thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Bad Request", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RestResponse.error(500, "Internal Server Error",
                            "An error occurred while updating showtime"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<Void>> deleteShowtime(@PathVariable Integer id) {
        try {
            showtimeService.deleteShowtime(id);
            return ResponseEntity.ok(RestResponse.success(null, "Xoá suất chiếu thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Bad Request", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RestResponse.error(500, "Internal Server Error",
                            "An error occurred while deleting showtime"));
        }
    }
}
