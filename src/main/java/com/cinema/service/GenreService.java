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

    GenreRepository genreRepository;
    GenreMapper genreMapper;
    MovieRepository movieRepository;

    /*
     * Lấy tất cả các thể loại phim
     * 
     * @return Danh sách các thể loại phim
     */
    public List<GenreDto> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(genreMapper::toDto)
                .collect(Collectors.toList());
    }

    /*
     * Tạo thể loại mới
     * 
     * @param genreDto: Thông tin thể loại mới
     * 
     * @return Thể loại mới
     * 
     * @throws RuntimeException nếu thể loại đã tồn tại
     */
    @Transactional
    public GenreDto createGenre(GenreDto genreDto) {
        if (genreRepository.existsByNameIgnoreCase(genreDto.getName())) {
            throw new RuntimeException("Thể loại này đã tồn tại.");
        }
        Genre genre = genreMapper.toEntity(genreDto);
        Genre saved = genreRepository.save(genre);
        return genreMapper.toDto(saved);
    }

    /*
     * Cập nhật thể loại
     * 
     * @param id: ID của thể loại cần cập nhật
     * 
     * @param genreDto: Thông tin thể loại cần cập nhật
     * 
     * @return Thể loại đã cập nhật
     * 
     * @throws RuntimeException nếu không tìm thấy thể loại hoặc thể loại đã tồn tại
     */
    @Transactional
    public GenreDto updateGenre(Integer id, GenreDto genreDto) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found"));
        if (genreRepository.existsByNameIgnoreCaseAndIdNot(genreDto.getName(), id)) {
            throw new RuntimeException("Thể loại này đã tồn tại");
        }
        genreMapper.updateEntity(genreDto, genre);
        Genre saved = genreRepository.save(genre);
        return genreMapper.toDto(saved);
    }

    /*
     * Xoá thể loại
     * 
     * @param id: ID của thể loại cần xoá
     * 
     * @throws RuntimeException nếu không tìm thấy thể loại hoặc thể loại đang được
     * sử dụng
     */
    @Transactional
    public void deleteGenre(Integer id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found"));
        if (movieRepository.existsByGenresId(id)) {
            throw new RuntimeException(
                    "Không thể xoá thể loại đang được sử dụng. Vui lòng gỡ khỏi các phim liên quan trước.");
        }
        genreRepository.delete(genre);
    }

}
