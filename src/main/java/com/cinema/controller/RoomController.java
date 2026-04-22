package com.cinema.controller;

import com.cinema.dto.response.RestResponse;
import com.cinema.dto.RoomDto;
import com.cinema.dto.SeatDto;
import com.cinema.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping
    public ResponseEntity<RestResponse<List<RoomDto>>> getAllRooms() {
        List<RoomDto> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(RestResponse.success(rooms, "Fetched rooms successfully"));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<RestResponse<List<SeatDto>>> getRoomSeats(@PathVariable Integer id) {
        List<SeatDto> seats = roomService.getRoomSeats(id);
        return ResponseEntity.ok(RestResponse.success(seats, "Fetched seats successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<RoomDto>> createRoom(@RequestBody RoomDto roomDto) {
        RoomDto createdRoom = roomService.createRoom(roomDto);
        return ResponseEntity.ok(RestResponse.success(createdRoom, "Created room successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<RoomDto>> getRoomById(@PathVariable Integer id) {
        RoomDto room = roomService.getRoomById(id);
        return ResponseEntity.ok(RestResponse.success(room, "Fetched room details successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<RoomDto>> updateRoom(
            @PathVariable Integer id,
            @RequestBody RoomDto roomDto) {
        RoomDto updatedRoom = roomService.updateRoom(id, roomDto);
        return ResponseEntity.ok(RestResponse.success(updatedRoom, "Updated room successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<RestResponse<Void>> deleteRoom(@PathVariable Integer id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(RestResponse.success(null, "Deleted room successfully"));
    }
}
