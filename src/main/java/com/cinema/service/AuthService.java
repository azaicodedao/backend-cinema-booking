package com.cinema.service;

import com.cinema.entity.RefreshToken;
import com.cinema.enums.Role;
import com.cinema.entity.User;
import com.cinema.dto.request.LoginRequest;
import com.cinema.dto.request.SignupRequest;
import com.cinema.dto.response.TokenResponse;
import com.cinema.repository.RefreshTokenRepository;
import com.cinema.repository.UserRepository;
import com.cinema.security.jwt.JwtUtils;
import com.cinema.security.services.UserDetailsImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.cinema.enums.UserStatus;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthService {

    final AuthenticationManager authenticationManager;
    final UserRepository userRepository;
    final RefreshTokenRepository refreshTokenRepository;
    final PasswordEncoder encoder;
    final JwtUtils jwtUtils;

    @Value("${jwt.refreshExpirationDay:7}")
    long refreshExpirationDays;

    @Transactional
    public TokenResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        refreshTokenRepository.deleteByUser_Id(user.getId());

        String refreshTokenString = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenString)
                .expiryDate(LocalDateTime.now().plusDays(refreshExpirationDays))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return TokenResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshTokenString)
                .user(TokenResponse.UserInfo.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .role(user.getRole().name())
                        .build())
                .build();
    }

    public String registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setFullName(signUpRequest.getFullName());
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(Role.CUSTOMER);

        userRepository.save(user);

        return "User registered successfully!";
    }

    @Transactional
    public TokenResponse refreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (refreshToken.getIsRevoked()) {
            throw new IllegalArgumentException("Refresh token was revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token is expired. Please sign in again");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtUtils.generateRefreshToken(user.getEmail(), 15 * 60 * 1000); 

        List<String> roles = List.of(user.getRole().name());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(token)
                .user(TokenResponse.UserInfo.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .role(user.getRole().name())
                        .build())
                .build();
    }

    @Transactional
    public void logout(Integer userId) {
        refreshTokenRepository.deleteByUser_Id(userId);
    }
}
