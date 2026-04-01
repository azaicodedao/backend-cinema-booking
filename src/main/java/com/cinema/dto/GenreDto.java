package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO chứa thông tin về thể loại phim (ví dụ: Hành động, Kinh dị, Tâm lý).
 */
public class GenreDto {
    private Integer id;
    private String name;
}
