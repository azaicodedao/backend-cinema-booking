package com.cinema.service;

import com.cinema.dto.ChartDataDto;
import com.cinema.dto.DailyRevenueDto;
import com.cinema.dto.MovieBookingStatDto;
import com.cinema.dto.SeatStatusMessageDto;
import com.cinema.dto.ShowtimeStatDto;
import com.cinema.dto.response.MovieBookingStatsResponseDto;
import com.cinema.enums.BookingStatus;
import com.cinema.repository.BookingRepository;
import com.cinema.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Service xử lý toàn bộ các logic nghiệp vụ liên quan đến thống kê dữ liệu đặt
 * vé,
 * doanh thu hàng ngày, doanh thu biểu đồ và trạng thái ghế phòng chiếu.
 */
@Service
@RequiredArgsConstructor
public class StatisticService {

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatHoldingService seatHoldingService;

    /**
     * Lấy dữ liệu thống kê đặt vé và doanh thu theo phim (bảng xếp hạng doanh thu
     * phim).
     *
     * @param startDate Ngày bắt đầu lọc thống kê
     * @param endDate   Ngày kết thúc lọc thống kê
     * @param movieId   ID của phim cần lọc (nếu lọc cụ thể cho một phim)
     * @return DTO chứa danh sách thống kê top 10 phim, tổng lượt đặt vé và tổng
     *         doanh thu toàn cục
     */
    public MovieBookingStatsResponseDto getMovieBookingStats(LocalDate startDate, LocalDate endDate, Integer movieId) {
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59) : LocalDateTime.now().plusYears(10);

        // Lấy danh sách Top 10 phim có doanh thu cao nhất dưới dạng Pageable
        Pageable pageable = PageRequest.of(0, 10);
        Page<MovieBookingStatDto> statsPage = bookingRepository.getMovieBookingStats(
                BookingStatus.CONFIRMED,
                start,
                end,
                movieId,
                pageable);

        // Lấy thống kê tổng quát toàn cục (Tổng lượt đặt và Tổng doanh thu)
        List<Object[]> globalStatsResult = bookingRepository.getGlobalBookingStats(
                BookingStatus.CONFIRMED,
                start,
                end,
                movieId);

        Long totalBookings = 0L;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        if (globalStatsResult != null && !globalStatsResult.isEmpty()) {
            Object[] row = globalStatsResult.get(0);
            if (row != null && row.length >= 2) {
                totalBookings = toLong(row[0]);
                totalRevenue = toBigDecimal(row[1]);
            }
        }

        return new MovieBookingStatsResponseDto(statsPage.getContent(), totalBookings, totalRevenue);
    }

    /**
     * Lấy dữ liệu thống kê trạng thái của các suất chiếu đã diễn ra (endTime < thời
     * điểm hiện tại).
     *
     * @param page      Số trang hiện tại (0-indexed)
     * @param size      Số lượng bản ghi trên một trang
     * @param startDate Ngày bắt đầu lọc khoảng thời gian suất chiếu
     * @param endDate   Ngày kết thúc lọc khoảng thời gian suất chiếu
     * @param movieId   ID phim để lọc
     * @param roomId    ID phòng chiếu để lọc
     * @return Trang (Page) chứa danh sách thống kê các suất chiếu
     */
    public Page<ShowtimeStatDto> getShowtimeStats(
            int page,
            int size,
            LocalDate startDate,
            LocalDate endDate,
            Integer movieId,
            Integer roomId) {

        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59) : null;
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(page, size);

        return showtimeRepository.getShowtimeStats(now, movieId, roomId, start, end, pageable);
    }

    /**
     * Lấy trạng thái đặt ghế (Trống, Đang được giữ, Đã bán) của phòng chiếu cho một
     * suất chiếu cụ thể.
     *
     * @param id ID của suất chiếu cần thống kê ghế
     * @return Danh sách các trạng thái của từng ghế trong phòng chiếu
     */
    public List<SeatStatusMessageDto> getShowtimeSeatStats(Integer id) {
        return seatHoldingService.getSeatsStatusForShowtime(id);
    }

    /**
     * Tính toán doanh thu chi tiết theo từng ngày trong khoảng thời gian xác định.
     * Dữ liệu trả về sẽ tự động bù đắp các ngày không có giao dịch với doanh thu =
     * 0.
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public List<DailyRevenueDto> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        List<Object[]> rawData = bookingRepository.getRawRevenueByDate(BookingStatus.CONFIRMED, start, end);

        // Tạo bản đồ lưu trữ với mọi ngày trong khoảng lọc, mặc định doanh thu = 0
        Map<LocalDate, BigDecimal> dateRevenueMap = new TreeMap<>();
        LocalDate curr = startDate;
        while (!curr.isAfter(endDate)) {
            dateRevenueMap.put(curr, BigDecimal.ZERO);
            curr = curr.plusDays(1);
        }

        // Duyệt qua kết quả thô từ CSDL và cộng dồn doanh thu thực tế vào bản đồ
        for (Object[] row : rawData) {
            if (row != null && row.length >= 2 && row[0] != null) {
                LocalDate date = ((LocalDateTime) row[0]).toLocalDate();
                dateRevenueMap.put(date, dateRevenueMap.getOrDefault(date, BigDecimal.ZERO).add(toBigDecimal(row[1])));
            }
        }

        List<DailyRevenueDto> result = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> entry : dateRevenueMap.entrySet()) {
            result.add(new DailyRevenueDto(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    /**
     * Lấy dữ liệu doanh thu gộp nhóm để hiển thị biểu đồ theo khoảng thời gian tùy
     * chọn.
     * Hỗ trợ 4 khoảng chu kỳ: 7 ngày qua, 30 ngày qua, 12 tuần qua, 12 tháng qua.
     *
     * @param period Chu kỳ gộp dữ liệu biểu đồ (7_days, 30_days, 12_weeks,
     *               12_months)
     * @return Danh sách mốc dữ liệu biểu đồ đã được gộp nhóm và điền sẵn dữ liệu
     *         mặc định bằng 0
     */
    public List<ChartDataDto> getRevenueChart(String period) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;

        // Xác định ngày bắt đầu dựa vào chu kỳ gộp nhóm
        switch (period) {
            case "30_days" -> startDate = today.minusDays(29);
            case "12_weeks" -> startDate = today.minusWeeks(11).with(DayOfWeek.MONDAY);
            case "12_months" -> startDate = today.minusMonths(11).withDayOfMonth(1);
            default -> startDate = today.minusDays(6); // Mặc định là 7_days
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);
        List<Object[]> rawData = bookingRepository.getRawRevenueByDate(BookingStatus.CONFIRMED, start, end);

        // Gọi hàm gom nhóm phù hợp tương ứng với chu kỳ lựa chọn
        return switch (period) {
            case "12_weeks" -> groupByWeek(rawData, startDate, today);
            case "12_months" -> groupByMonth(rawData, startDate, today);
            default -> groupByDay(rawData, startDate, today);
        };
    }

    /**
     * Gom nhóm dữ liệu doanh thu thô theo Ngày.
     * Định dạng nhãn trục hoành biểu đồ là "dd/MM" (Ví dụ: "21/05").
     */
    private List<ChartDataDto> groupByDay(List<Object[]> rawData, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, BigDecimal> map = new LinkedHashMap<>();
        LocalDate curr = startDate;
        while (!curr.isAfter(endDate)) {
            map.put(curr, BigDecimal.ZERO);
            curr = curr.plusDays(1);
        }

        for (Object[] row : rawData) {
            if (row != null && row.length >= 2 && row[0] != null) {
                LocalDate date = ((LocalDateTime) row[0]).toLocalDate();
                map.merge(date, toBigDecimal(row[1]), BigDecimal::add);
            }
        }

        return map.entrySet().stream()
                .map(e -> new ChartDataDto(e.getKey().format(DateTimeFormatter.ofPattern("dd/MM")), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Gom nhóm dữ liệu doanh thu thô theo Tuần (T1, T2,... T53).
     * Định dạng nhãn trục hoành biểu đồ là "T[Số_tuần]" (Ví dụ: "T21").
     */
    private List<ChartDataDto> groupByWeek(List<Object[]> rawData, LocalDate startDate, LocalDate endDate) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();

        // Khởi tạo trước các mốc tuần trong khoảng lọc để tránh thiếu tuần
        LocalDate weekStart = startDate;
        while (!weekStart.isAfter(endDate)) {
            int weekNum = weekStart.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            map.putIfAbsent("T" + weekNum, BigDecimal.ZERO);
            weekStart = weekStart.plusWeeks(1);
        }

        for (Object[] row : rawData) {
            if (row != null && row.length >= 2 && row[0] != null) {
                LocalDate date = ((LocalDateTime) row[0]).toLocalDate();
                int weekNum = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                map.merge("T" + weekNum, toBigDecimal(row[1]), BigDecimal::add);
            }
        }

        return map.entrySet().stream()
                .map(e -> new ChartDataDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Gom nhóm dữ liệu doanh thu thô theo Tháng (Th1/26, Th2/26,...).
     * Định dạng nhãn trục hoành biểu đồ là "Th[Tháng]/[Năm_rút_gọn]" (Ví dụ:
     * "Th5/26").
     */
    private List<ChartDataDto> groupByMonth(List<Object[]> rawData, LocalDate startDate, LocalDate endDate) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();

        // Khởi tạo trước các mốc tháng trong khoảng lọc
        YearMonth currMonth = YearMonth.from(startDate);
        YearMonth lastMonth = YearMonth.from(endDate);
        while (!currMonth.isAfter(lastMonth)) {
            String key = "Th" + currMonth.getMonthValue() + "/" + (currMonth.getYear() % 100);
            map.put(key, BigDecimal.ZERO);
            currMonth = currMonth.plusMonths(1);
        }

        for (Object[] row : rawData) {
            if (row != null && row.length >= 2 && row[0] != null) {
                LocalDate date = ((LocalDateTime) row[0]).toLocalDate();
                YearMonth ym = YearMonth.from(date);
                String key = "Th" + ym.getMonthValue() + "/" + (ym.getYear() % 100);
                map.merge(key, toBigDecimal(row[1]), BigDecimal::add);
            }
        }

        return map.entrySet().stream()
                .map(e -> new ChartDataDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi dữ liệu Object thô từ CSDL sang kiểu BigDecimal an toàn.
     * Hỗ trợ các trường hợp dữ liệu thô trả về là BigDecimal hoặc các lớp con của
     * Number (Double, Long, Integer).
     *
     * @param obj Đối tượng giá trị tiền tệ thô
     * @return Giá trị BigDecimal tương ứng hoặc BigDecimal.ZERO nếu null/không hợp
     *         lệ
     */
    private BigDecimal toBigDecimal(Object obj) {
        if (obj instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (obj instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.ZERO;
    }

    /**
     * Chuyển đổi dữ liệu Object thô sang kiểu Long an toàn.
     * Thường dùng để lấy tổng số lượng đặt vé thô từ hàm SUM/COUNT của SQL.
     *
     * @param obj Đối tượng số lượng thô
     * @return Giá trị Long tương ứng hoặc 0L nếu null/không hợp lệ
     */
    private Long toLong(Object obj) {
        if (obj instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }
}
