package com.cinema.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO đại diện cho một trang dữ liệu trả về từ API, bao gồm danh sách các đối tượng, tổng số bản ghi, trang hiện tại và tổng số trang.
 */
public class PageResponse<T> {
    private List<T> items;
    private long total;
    private int page;
    private int totalPages;
}
