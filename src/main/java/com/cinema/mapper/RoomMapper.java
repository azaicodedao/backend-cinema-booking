package com.cinema.mapper;

import com.cinema.dto.RoomDto;
import com.cinema.entity.Room;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    RoomDto toDto(Room room);
    Room toEntity(RoomDto roomDto);
}
