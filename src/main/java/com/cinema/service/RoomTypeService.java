package com.cinema.service;

import com.cinema.dto.RoomTypeDto;
import com.cinema.entity.RoomType;
import com.cinema.repository.RoomTypeRepository;
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
public class RoomTypeService {

    RoomTypeRepository roomTypeRepository;

    @Transactional(readOnly = true)
    public List<RoomTypeDto> getAllRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomTypeDto createRoomType(RoomTypeDto dto) {
        RoomType entity = new RoomType();
        entity.setName(dto.getName());
        entity.setSurcharge(dto.getSurcharge() != null ? dto.getSurcharge() : java.math.BigDecimal.ZERO);
        RoomType saved = roomTypeRepository.save(entity);
        return convertToDto(saved);
    }

    @Transactional
    public RoomTypeDto updateRoomType(Integer id, RoomTypeDto dto) {
        RoomType entity = roomTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room Type not found with id: " + id));
        entity.setName(dto.getName());
        entity.setSurcharge(dto.getSurcharge() != null ? dto.getSurcharge() : java.math.BigDecimal.ZERO);
        RoomType updated = roomTypeRepository.save(entity);
        return convertToDto(updated);
    }

    @Transactional
    public void deleteRoomType(Integer id) {
        if (!roomTypeRepository.existsById(id)) {
            throw new IllegalArgumentException("Room Type not found with id: " + id);
        }
        roomTypeRepository.deleteById(id);
    }

    private RoomTypeDto convertToDto(RoomType entity) {
        return RoomTypeDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .surcharge(entity.getSurcharge())
                .build();
    }
}
