package com.cinema.mapper;

import com.cinema.dto.response.AdminUserResponse;
import com.cinema.dto.response.UserProfileResponse;
import com.cinema.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper chuyển đổi User entity sang các DTO response an toàn.
 * Đảm bảo password không bao giờ bị serialize ra ngoài API.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "gender", expression = "java(user.getGender() != null ? user.getGender().name() : null)")
    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().name() : null)")
    UserProfileResponse toProfileResponse(User user);

    @Mapping(target = "gender", expression = "java(user.getGender() != null ? user.getGender().name() : null)")
    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().name() : null)")
    @Mapping(target = "status", expression = "java(user.getStatus() != null ? user.getStatus().name() : null)")
    AdminUserResponse toAdminResponse(User user);
}
