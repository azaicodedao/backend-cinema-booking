package com.cinema.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Kích hoạt WebSocket với message broker (STOMP)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");// Kích hoạt in-memory message broker để broadcast message đến các client
                                            // đang subcribe kênh /topic/...
        config.setApplicationDestinationPrefixes("/app");// Định nghĩa prefix cho các message gửi từ client đến server.
                                                         // Khi client gửi đến /app/something, Spring sẽ route đến
                                                         // @MessageMapping("/something") trong controller.
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-cinema").setAllowedOriginPatterns("*").withSockJS();
        // Định nghĩa URL endpoint mà WebSocket client kết nối vào:
        // ws://localhost:8080/ws-cinema
        // Cho phép kết nối từ mọi origin (CORS). Nên giới hạn lại ở production.
        // Bật SockJS fallback: nếu trình duyệt không hỗ trợ WebSocket thuần, nó sẽ tự
        // động dùng các phương thức dự phòng (long-polling, iframe...).
    }
}
/*
 * File này cấu hình WebSocket cho ứng dụng cinema, sử dụng giao thức STOMP
 * (Simple Text Oriented Messaging Protocol) trên nền SockJS – cho phép server
 * đẩy dữ liệu real-time đến client (ví dụ: thông báo ghế đang bị giữ, đặt vé
 * thành công...).
 */
