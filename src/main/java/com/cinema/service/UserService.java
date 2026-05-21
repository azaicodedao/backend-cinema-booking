package com.cinema.service;

import com.cinema.entity.User;
import com.cinema.dto.request.ChangePasswordRequest;
import com.cinema.dto.request.UpdateProfileRequest;
import com.cinema.dto.response.UserProfileResponse;
import com.cinema.mapper.UserMapper;
import com.cinema.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;

    /**
     * Lấy thông tin cá nhân của người dùng thông qua ID.
     * Kiểm tra tính hợp lệ của ID trước khi trả về thông tin.
     */
    public UserProfileResponse getProfile(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userMapper.toProfileResponse(user);
    }

    /**
     * Cập nhật thông tin cá nhân của người dùng.
     * Bao gồm tên, số điện thoại, giới tính, địa chỉ và URL ảnh đại diện.
     * 
     * @param userId  ID của người dùng cần cập nhật.
     * @param request Đối tượng chứa thông tin cần cập nhật.
     * @return Trả về UserProfileResponse chứa thông tin đã cập nhật.
     */

    @Transactional
    public UserProfileResponse updateProfile(Integer userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            try {
                user.setGender(com.cinema.enums.Gender.valueOf(request.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {

            }
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        User saved = userRepository.save(user);
        return userMapper.toProfileResponse(saved);
    }

    /**
     * Thay đổi mật khẩu của người dùng.
     * Yêu cầu xác thực mật khẩu cũ và đảm bảo mật khẩu mới không trùng với mật khẩu
     * cũ.
     * 
     * @param userId  ID của người dùng cần thay đổi mật khẩu.
     * @param request Đối tượng chứa mật khẩu cũ, mật khẩu mới và xác nhận mật khẩu
     *                mới.
     */
    @Transactional
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu cũ");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
