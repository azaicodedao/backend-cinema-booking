package com.cinema.controller;

import com.cinema.dto.SeatSelectionDto;
import com.cinema.dto.SeatStatusMessageDto;
import com.cinema.dto.request.SeatHoldRequest;
import com.cinema.dto.response.RestResponse;
import com.cinema.security.services.UserDetailsImpl;
import com.cinema.service.SeatHoldingService;
import com.cinema.service.SeatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatController {

    SeatService seatService;
    SeatHoldingService seatHoldingService;

    @GetMapping("/showtime/{id}")
    public ResponseEntity<RestResponse<SeatSelectionDto>> getSeatSelection(@PathVariable Integer id) {
        try {
            SeatSelectionDto selection = seatService.getSeatSelection(id);
            return ResponseEntity.ok(RestResponse.success(selection, "Fetched seat selection successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(RestResponse.error(400, "Bad Request", e.getMessage()));
        }
    }

    @PostMapping("/hold")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<RestResponse<List<SeatStatusMessageDto>>> holdSeats(@RequestBody SeatHoldRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            List<SeatStatusMessageDto> messages = seatHoldingService.holdSeats(request.getSeatIds(), request.getShowtimeId(), userDetails.getId());
            return ResponseEntity.ok(RestResponse.success(messages, "Seats held successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(RestResponse.error(400, "Bad Request", e.getMessage()));
        }
    }

    @PostMapping("/release")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<RestResponse<List<SeatStatusMessageDto>>> releaseSeats(@RequestBody SeatHoldRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            List<SeatStatusMessageDto> messages = seatHoldingService.releaseSeats(request.getSeatIds(), request.getShowtimeId(), userDetails.getId());
            return ResponseEntity.ok(RestResponse.success(messages, "Seats released successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(RestResponse.error(400, "Bad Request", e.getMessage()));
        }
    }
}
