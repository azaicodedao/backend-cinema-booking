package com.cinema.service;

import com.cinema.dto.RoomDto;
import com.cinema.dto.SeatDto;
import com.cinema.entity.Room;
import com.cinema.entity.Seat;
import com.cinema.mapper.RoomMapper;
import com.cinema.mapper.SeatMapper;
import com.cinema.repository.RoomRepository;
import com.cinema.repository.RoomTypeRepository;
import com.cinema.repository.SeatRepository;
import com.cinema.repository.ShowtimeRepository;
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
    RoomTypeRepository roomTypeRepository;
    SeatRepository seatRepository;
    RoomMapper roomMapper;
    SeatMapper seatMapper;
    ShowtimeRepository showtimeRepository;

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

    @Transactional
    public RoomDto updateRoom(Integer id, RoomDto roomDto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với ID: " + id));
        if (roomDto.getName() != null) {
            room.setName(roomDto.getName());
        }
        if (roomDto.getType() != null) {
            room.setRoomType(roomTypeRepository.findByName(roomDto.getType())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy loại phòng: " + roomDto.getType())));
        }
        if (roomDto.getStatus() != null) {
            room.setStatus(com.cinema.enums.RoomStatus.valueOf(roomDto.getStatus()));
        }
        Room savedRoom = roomRepository.save(room);
        return roomMapper.toDto(savedRoom);
    }

    // Hiển thị
    public RoomDto getRoomById(Integer id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
        return roomMapper.toDto(room);
    }

    // Xóa phòng
    @Transactional
    public void deleteRoom(Integer id) {
        if (showtimeRepository.existsByRoomId(id)) {
            throw new RuntimeException("Không thể xóa phòng vì phòng có suất chiếu");
        }
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với ID: " + id));
        roomRepository.delete(room);
    }
}
