package com.cinema.service;

import com.cinema.dto.GenreDto;
import com.cinema.entity.Genre;
import com.cinema.mapper.GenreMapper;
import com.cinema.repository.GenreRepository;
import com.cinema.repository.MovieRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenreService {

    GenreRepository GenreRepository;
    GenreMapper GenreMapper;
    MovieRepository movieRepository;

    public List<GenreDto> getAllGenres() {
        return GenreRepository.findAll().stream()
                .map(GenreMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public GenreDto createGenre(GenreDto GenreDto) {
        Genre Genre = GenreMapper.toEntity(GenreDto);
        Genre saved = GenreRepository.save(Genre);
        return GenreMapper.toDto(saved);
    }

    @Transactional
    public GenreDto updateGenre(Integer id, GenreDto GenreDto) {
        Genre Genre = GenreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found"));
//        if(GenreDto.getName() != null) {
//            Genre.setName(GenreDto.getName());
//        }
        GenreMapper.updateEntity(GenreDto, Genre);
        Genre saved = GenreRepository.save(Genre);
        return GenreMapper.toDto(saved);
    }

    @Transactional
    public void deleteGenre(Integer id) {
        Genre genre = GenreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found"));
        if(movieRepository.existsByGenresId(id)) {
            throw new RuntimeException("Không thể xóa thể loại, vì có bộ phim đang thuộc thể loại này");
        }
        GenreRepository.delete(genre);
    }
}
