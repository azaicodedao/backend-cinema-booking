package com.cinema.service;

import com.cinema.dto.GenreDto;
import com.cinema.entity.Genre;
import com.cinema.mapper.GenreMapper;
import com.cinema.repository.GenreRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenreService {

    GenreRepository GenreRepository;
    GenreMapper GenreMapper;

    public List<GenreDto> getAllGenres() {
        return GenreRepository.findAll().stream()
                .map(GenreMapper::toDto)
                .collect(Collectors.toList());
    }

    public GenreDto createGenre(GenreDto GenreDto) {
        Genre Genre = GenreMapper.toEntity(GenreDto);
        Genre saved = GenreRepository.save(Genre);
        return GenreMapper.toDto(saved);
    }

    public GenreDto updateGenre(Integer id, GenreDto GenreDto) {
        Genre Genre = GenreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found"));
        GenreMapper.updateEntity(GenreDto, Genre);
        Genre saved = GenreRepository.save(Genre);
        return GenreMapper.toDto(saved);
    }
}
