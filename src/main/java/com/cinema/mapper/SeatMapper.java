package com.cinema.mapper;

import com.cinema.dto.SeatDto;
import com.cinema.entity.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    @Mapping(source = "room.id", target = "roomId")
    SeatDto toDto(Seat seat);
    
}
