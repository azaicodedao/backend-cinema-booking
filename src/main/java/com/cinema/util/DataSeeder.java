package com.cinema.util;

import com.cinema.enums.Role;
import com.cinema.entity.User;
import com.cinema.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (!userRepository.existsByEmail("admin@cinema.com")) {
            log.info("Khởi tạo tài khoản Admin mặc định...");
            User adminUser = User.builder()
                    .fullName("System Admin")
                    .email("admin@cinema.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(adminUser);
            log.info("Đã tạo tài khoản Admin - Email: admin@cinema.com / Password: admin123");
        } else {
            log.info("Tài khoản admin đã tồn tại.");
        }
    }
}
