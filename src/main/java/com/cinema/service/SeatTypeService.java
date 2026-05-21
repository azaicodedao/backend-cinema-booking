package com.cinema.service;

import com.cinema.dto.SeatTypeDto;
import com.cinema.entity.SeatType;
import com.cinema.repository.SeatTypeRepository;
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
public class SeatTypeService {

    SeatTypeRepository seatTypeRepository;

    @Transactional(readOnly = true)
    public List<SeatTypeDto> getAllSeatTypes() {
        return seatTypeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SeatTypeDto createSeatType(SeatTypeDto dto) {
        SeatType entity = new SeatType();
        entity.setName(dto.getName());
        entity.setSurcharge(dto.getSurcharge() != null ? dto.getSurcharge() : java.math.BigDecimal.ZERO);
        SeatType saved = seatTypeRepository.save(entity);
        return convertToDto(saved);
    }

    @Transactional
    public SeatTypeDto updateSeatType(Integer id, SeatTypeDto dto) {
        SeatType entity = seatTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seat Type not found with id: " + id));
        entity.setName(dto.getName());
        entity.setSurcharge(dto.getSurcharge() != null ? dto.getSurcharge() : java.math.BigDecimal.ZERO);
        SeatType updated = seatTypeRepository.save(entity);
        return convertToDto(updated);
    }

    @Transactional
    public void deleteSeatType(Integer id) {
        if (!seatTypeRepository.existsById(id)) {
            throw new IllegalArgumentException("Seat Type not found with id: " + id);
        }
        seatTypeRepository.deleteById(id);
    }

    private SeatTypeDto convertToDto(SeatType entity) {
        return SeatTypeDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .surcharge(entity.getSurcharge())
                .build();
    }
}
