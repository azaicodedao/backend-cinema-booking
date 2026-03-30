package com.cinema.service;

import com.cinema.dto.RoomDto;
import com.cinema.dto.SeatDto;
import com.cinema.entity.Room;
import com.cinema.entity.Seat;
import com.cinema.mapper.RoomMapper;
import com.cinema.mapper.SeatMapper;
import com.cinema.repository.RoomRepository;
import com.cinema.repository.SeatRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomService {

    RoomRepository roomRepository;
    SeatRepository seatRepository;
    RoomMapper roomMapper;
    SeatMapper seatMapper;

    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(roomMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<SeatDto> getRoomSeats(Integer roomId) {
        return seatRepository.findByRoomId(roomId).stream()
                .map(seatMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomDto createRoom(RoomDto roomDto) {
        Room room = roomMapper.toEntity(roomDto);
        Room savedRoom = roomRepository.save(room);

        for (int i = 0; i < savedRoom.getTotalRows(); i++) {
            char rowChar = (char) ('A' + i);
            for (int j = 1; j <= savedRoom.getTotalCols(); j++) {
                Seat seat = new Seat();
                seat.setRoom(savedRoom);
                seat.setRowLabel(String.valueOf(rowChar));
                seat.setColNumber(j);
                seatRepository.save(seat);
            }
        }

        return roomMapper.toDto(savedRoom);
    }
}
