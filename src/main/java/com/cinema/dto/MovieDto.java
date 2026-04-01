package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO chứa thông tin cơ bản về phim, được sử dụng trong các tác vụ CRUD hoặc hiển thị danh sách đơn giản.
 */
public class MovieDto {
    private Integer id;
    private String title;
    private String description;
    private Integer duration;
    private Integer ageLimit;
    private String posterUrl;
    private boolean isActive;
    private String status;
    private List<GenreDto> Genres;
}
