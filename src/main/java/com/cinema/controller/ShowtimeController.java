package com.cinema.controller;

import com.cinema.dto.response.RestResponse;
import com.cinema.dto.ShowtimeDto;
import com.cinema.service.ShowtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<ShowtimeDto>> createShowtime(@RequestBody ShowtimeDto showtimeDto) {
        try {
            ShowtimeDto createdShowtime = showtimeService.createShowtime(showtimeDto);
            return ResponseEntity.ok(RestResponse.success(createdShowtime, "Created showtime successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Bad Request", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RestResponse.error(500, "Internal Server Error", "An error occurred while creating showtime"));
        }
    }
}
