package com.cinema.mapper;

import com.cinema.dto.SeatDto;
import com.cinema.entity.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "rowLabel", target = "rowLetter")
    @Mapping(source = "colNumber", target = "seatNumber")
    @Mapping(target = "typeName", source = "seatType.name")
    @Mapping(target = "surcharge", source = "seatType.surcharge")
    SeatDto toDto(Seat seat);

    @Mapping(target = "room", ignore = true)
    @Mapping(source = "rowLetter", target = "rowLabel")
    @Mapping(source = "seatNumber", target = "colNumber")
    @Mapping(target = "seatType", ignore = true)
    Seat toEntity(SeatDto seatDto);
}
