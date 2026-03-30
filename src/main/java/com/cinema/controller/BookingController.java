package com.cinema.controller;

import com.cinema.dto.response.RestResponse;
import com.cinema.dto.request.BookingRequestDto;
import com.cinema.entity.Booking;
import com.cinema.security.services.UserDetailsImpl;
import com.cinema.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<RestResponse<Booking>> createBooking(@RequestBody BookingRequestDto request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        try {
            Booking booking = bookingService.createBooking(request, userDetails.getId());
            return ResponseEntity.ok().body(RestResponse.success(booking, "Booking created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Bad Request", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RestResponse.error(500, "Internal Server Error", "An error occurred while creating booking"));
        }
    }

    @PostMapping("/{bookingId}/pay")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<RestResponse<Void>> payBooking(@PathVariable Integer bookingId) {
        try {
            bookingService.payBooking(bookingId);
            return ResponseEntity.ok().body(RestResponse.<Void>success(null, "Booking Paid Successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Bad Request", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RestResponse.error(500, "Internal Server Error", "An error occurred while paying booking"));
        }
    }
}
