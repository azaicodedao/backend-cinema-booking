package com.cinema.security.services;

import com.cinema.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
/**
 * Vấn đề là Spring Security không hề biết thực thể User mà bạn tạo ra trong
 * Database có hình thù ra sao (có trường email, fullName hay id gì không). Nó
 * chỉ làm việc và hiểu một thứ duy nhất: Interface UserDetails
 * Do đó, bạn phải tạo ra lớp UserDetailsImpl (kế thừa UserDetails) để \"dịch\"
 * dữ liệu của bạn sang chuẩn của Spring Security.
 */
@Getter
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String username; // ánh xạ từ email
    private String fullName;
    @JsonIgnore
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().name()));
        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPassword(),
                authorities);
    }

    /*
     * Spring Security cung cấp 4 hàm để kiểm tra trạng thái tài khoản.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /* Tài khoản chưa hết hạn. */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /*Tài khoản không bị khóa. */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /* Thông tin xác thực (mật khẩu) chưa hết hạn. */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /* Tài khoản đang được kích hoạt/hoạt động bình thường. */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Hàm này được ghi đè (override) để nói cho Java biết cách so sánh 2 đối tượng
     * UserDetailsImpl.
     * 
     * Nó so sánh dựa trên id.
     * 
     * Nếu hai đối tượng có cùng id, hệ thống sẽ coi đó là cùng một người dùng, bất
     * kể tên tuổi hay email bên trong có bị thay đổi hay không. Điều này rất hữu
     * ích cho các cấu trúc dữ liệu của Java (như Set, Map) hoặc khi Spring Security
     * cần xác minh người dùng trong bộ nhớ cache.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
