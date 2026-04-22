package com.cinema.controller;

import com.cinema.dto.response.RestResponse;
import com.cinema.dto.BookingResponseDTO;
import com.cinema.dto.BookingDetailDTO;
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
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Controller xử lý các yêu cầu HTTP liên quan đến đặt vé (Booking).
 * Cung cấp các API: Tạo đơn hàng, thanh toán và lấy chi tiết đơn hàng cho khách hàng.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<RestResponse<Map<String, Object>>> createBooking(@RequestBody BookingRequestDto request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        try {
            // Service tạo đơn hàng và trả về Entity chính
            Booking booking = bookingService.createBooking(request, userDetails.getId());
            
            // CHÚ Ý: Không trả về trực tiếp Entity (Booking) vì dễ gặp lỗi Hibernate Proxy (Serialization).
            // Chúng ta đóng gói kết quả vào một Map (DTO đơn giản) chỉ chứa ID.
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("bookingId", booking.getId());
            
            return ResponseEntity.ok().body(RestResponse.success(responseData, "Booking created successfully"));
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
    public ResponseEntity<RestResponse<Void>> payBooking(@PathVariable Integer bookingId,
                                                         @RequestParam(name = "paymentMethod", defaultValue = "VNPAY") String paymentMethod) {
        try {
            bookingService.payBooking(bookingId, paymentMethod);
            return ResponseEntity.ok().body(RestResponse.<Void>success(null, "Booking Paid Successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Bad Request", e.getMessage()));
        } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                     .body(RestResponse.error(500, "Internal Server Error", "An error occurred while paying booking"));
         }
     }
 
    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<RestResponse<BookingDetailDTO>> getBookingDetail(@PathVariable Integer bookingId) {
        try {
            BookingDetailDTO detail = bookingService.getBookingDetail(bookingId);
            return ResponseEntity.ok().body(RestResponse.success(detail, "Booking detail retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(RestResponse.error(404, "Not Found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RestResponse.error(500, "Internal Server Error", "Internal server error"));
        }
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<RestResponse<List<BookingResponseDTO>>> getMyBookings() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            List<BookingResponseDTO> bookings = bookingService.getUserBookings(userDetails.getId());
            return ResponseEntity.ok().body(RestResponse.success(bookings, "User bookings retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RestResponse.error(500, "Internal Server Error", "An error occurred while fetching your bookings"));
        }
    }
}
