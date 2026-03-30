package com.cinema.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    /*
     * Đây là "chìa khóa bí mật" (secret key) dùng để ký (sign) thuật toán. Ai có
     * khóa này mới có thể tạo ra token hợp lệ.
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /*
     * Thời gian sống của Access Token (tính bằng mili-giây). Ví dụ: 86400000 tương
     * đương 24 giờ.
     */
    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Hàm này lấy chuỗi jwtSecret, chuyển nó thành mảng byte (getBytes()) và dùng
     * thuật toán HMAC-SHA để băm nó thành một đối tượng Key chuẩn mực. Việc dùng
     * đối tượng Key thay vì chuỗi String thuần túy là yêu cầu bắt buộc của các
     * phiên bản thư viện io.jsonwebtoken (JJWT) mới nhằm tăng cường bảo mật.
     */
    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Tạo JWT Access Token dựa trên thông tin Authentication (cụ thể là username).
     * Token này sẽ được dán nhãn ngày phát hành và ngày hết hạn.
     */
    public String generateJwtToken(Authentication authentication) {
        return Jwts.builder()
                .setSubject((authentication.getName()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                /* Nén tất cả lại thành một chuỗi Header.Payload.Signature hoàn chỉnh. */
                .compact();
    }

    /**
     * Hàm này đưa token vào "máy quét". Nó dùng chìa khóa key() để mở token. Nếu mở
     * thành công, nó sẽ lấy phần Subject (chính là username đã được nhét vào ở bước
     * tạo) và trả về.
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * Cung cấp một phương pháp linh hoạt để tạo ra JWT làm Refresh Token hoặc
     * Access Token tùy chỉnh
     * với một ngày hết hạn (`expirationMs`) cụ thể.
     */
    public String generateRefreshToken(String subject, long expirationMs) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                /* Nén tất cả lại thành một chuỗi Header.Payload.Signature hoàn chỉnh. */
                .compact();
    }

    /**
     * Kiểm tra tính hợp lệ của token do client gửi lên (token có bị sửa đổi hay hết
     * hạn, hỏng định dạng hay không).
     * Trả về true nếu hợp lệ ngược lại in ra lỗi và trả về false.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            /**
             * Khi bạn tạo token (ở hàm generateJwtToken), bạn đã gắn cho nó một mốc thời
             * gian hết hạn thông qua lệnh .setExpiration(...). Lệnh này sẽ tạo ra một
             * trường có tên là exp (Expiration time) nằm trong phần Payload của chuỗi JWT.
             * Khi hàm .parse(authToken) được gọi, thư viện JJWT sẽ làm các bước sau:
             * + Nó dùng chìa khóa bí mật (key()) để mở khóa chuỗi token.
             * + Nếu mở khóa thành công, nó sẽ tự động trích xuất trường exp ra.
             * + Nó lấy thời gian của trường exp đó so sánh trực tiếp với thời gian hiện tại
             * của máy chủ (Server).
             */
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
        }
        return false;
    }
}
