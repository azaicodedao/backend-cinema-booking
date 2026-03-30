package com.cinema.security.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    /**
     * Phương thức này được kích hoạt bất cứ khi nào một yêu cầu HTTP không được xác thực
     * cố gắng truy cập vào một tài nguyên bảo mật và ném ra AuthenticationException.
     * Chức năng của nó là phản hồi lại lỗi 401 Unauthorized thay vì điều hướng tới trang đăng nhập.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
    }
}
