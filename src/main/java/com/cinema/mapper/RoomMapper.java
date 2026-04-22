package com.cinema.mapper;

import com.cinema.dto.RoomDto;
import com.cinema.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    @Mapping(target = "type", source = "roomType.name")
    @Mapping(target = "surcharge", source = "roomType.surcharge")
    RoomDto toDto(Room room);

    @Mapping(target = "roomType", ignore = true)
    Room toEntity(RoomDto roomDto);
}
