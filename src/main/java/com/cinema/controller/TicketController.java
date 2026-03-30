package com.cinema.controller;

import com.cinema.dto.response.RestResponse;
import com.cinema.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping("/checkin/{ticketCode}")
    @PreAuthorize("hasAuthority('STAFF') or hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<Void>> checkInTicket(@PathVariable String ticketCode) {
        try {
            String message = ticketService.checkInTicket(ticketCode);
            return ResponseEntity.ok(RestResponse.<Void>success(null, message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.error(400, "Bad Request", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RestResponse.error(500, "Internal Server Error", "An error occurred while checking in ticket"));
        }
    }
}
