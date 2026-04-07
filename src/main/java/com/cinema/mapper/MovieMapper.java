package com.cinema.mapper;

import com.cinema.dto.MovieDetailDTO;
import com.cinema.dto.MovieDto;
import com.cinema.dto.MovieItemDTO;
import com.cinema.dto.GenreDto;
import com.cinema.entity.Movie;
import com.cinema.entity.Genre;

import com.cinema.enums.MovieStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = { GenreMapper.class })
public interface MovieMapper {

    @Mapping(target = "status", expression = "java(movie.getStatus() != null ? movie.getStatus().name() : null)")
    @Mapping(target = "posterUrl", ignore = true)
    @Mapping(target = "trailerUrl", source = "trailerUrl")
    MovieDto toDto(Movie movie);

    @Mapping(target = "status", expression = "java(movie.getStatus() != null ? movie.getStatus().name() : null)")
    @Mapping(target = "posterUrl", ignore = true)
    @Mapping(target = "trailerUrl", source = "trailerUrl")
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "ratingDistribution", ignore = true)
    @Mapping(target = "isFeatured", source = "isFeatured")
    MovieItemDTO toItemDto(Movie movie);

    @Mapping(target = "status", expression = "java(movie.getStatus() != null ? movie.getStatus().name() : null)")
    @Mapping(target = "posterUrl", ignore = true)
    @Mapping(target = "trailerUrl", source = "trailerUrl")
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "ratingDistribution", ignore = true)
    MovieDetailDTO toDetailDto(Movie movie);

    @Mapping(target = "status", expression = "java(movieDto.getStatus() != null ? com.cinema.enums.MovieStatus.valueOf(movieDto.getStatus()) : null)")
    @Mapping(target = "posterUrl", ignore = true)
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "director", ignore = true)
    @Mapping(target = "actors", ignore = true)
    @Mapping(target = "trailerUrl", ignore = true)
    @Mapping(target = "releaseDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Movie toEntity(MovieDto movieDto);

    @Mapping(target = "status", expression = "java(movieDto.getStatus() != null ? com.cinema.enums.MovieStatus.valueOf(movieDto.getStatus()) : null)")
    @Mapping(target = "posterUrl", ignore = true)
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "director", ignore = true)
    @Mapping(target = "actors", ignore = true)
    @Mapping(target = "trailerUrl", ignore = true)
    @Mapping(target = "releaseDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(MovieDto movieDto, @MappingTarget Movie movie);
}
