package com.cinema.mapper;

import com.cinema.dto.GenreDto;
import com.cinema.entity.Genre;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GenreMapper {
    GenreDto toDto(Genre Genre);
    Genre toEntity(GenreDto GenreDto);
    void updateEntity(GenreDto GenreDto, @MappingTarget Genre Genre);
}
