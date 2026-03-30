package com.cinema.config;

import com.cinema.security.jwt.AuthEntryPointJwt;
import com.cinema.security.jwt.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    /**
     * Tạo Bean cho bộ lọc xác thực JWT, giúp kiểm tra và xác thực token trong mỗi
     * HTTP request.
     */
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    /**
     * Cấu hình nhà cung cấp xác thực (AuthenticationProvider) sử dụng Custom
     * UserDetailsService và PasswordEncoder.
     */
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    /**
     * Tạo Bean AuthenticationManager để quản lý và xử lý quá trình xác thực của
     * Spring Security.
     */
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    /**
     * Tạo Bean PasswordEncoder sử dụng thuật toán BCrypt để mã hóa mật khẩu an
     * toàn.
     */
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    /**
     * Cấu hình chuỗi lọc bảo mật cốt lõi (SecurityFilterChain).
     * Tắt CSRF, cấu hình quản lý phiên (session) thành Stateless (vì dùng JWT),
     * thiết lập các quy tắc phân quyền cho request (cho phép truy cập public vào
     * API Auth, cấu hình endpoint)
     * và chèn bộ lọc JWT vào trước bộ lọc xác thực mặc định.
     */
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // Cho phép TẤT CẢ các request đi qua
        // .requestMatchers("/api/auth/**").permitAll()
        // .requestMatchers("/api/public/**").permitAll()
        // .requestMatchers("/ws/**").permitAll()
        // .anyRequest().authenticated());

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    /**
     * Cấu hình nguồn CORS (Cross-Origin Resource Sharing) để cho phép các frontend
     * (như React/Vue)
     * từ origin cụ thể được phép gọi các API của backend mà không bị chặn bởi trình
     * duyệt.
     */
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token")); // Cho phép họ
                                                                                                         // gửi/nhận cái
                                                                                                         // Header có
                                                                                                         // tên là
                                                                                                         // Authorization
                                                                                                         // (để chứa
                                                                                                         // token).
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
