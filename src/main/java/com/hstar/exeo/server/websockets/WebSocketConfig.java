package com.hstar.exeo.server.websockets;

import com.hstar.exeo.server.repos.DeviceRepository;
import com.hstar.exeo.server.security.jwt.JWTAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 *
 * Created by Saswat on 8/11/2015.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired private RedisTemplate<String, String> redis;
    @Autowired private RedisMessageListenerContainer listenerContainer;
    @Autowired private JWTAuthenticationProvider jwtAuthenticationProvider;
    @Autowired private DeviceRepository deviceRepository;
    @Autowired private ApplicationContext applicationContext;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //todo further investigate setAllowedOrigins and possible security flaws
        registry.addHandler(myHandler(), "/api/wsignaler").setAllowedOrigins("*").withSockJS();
                            //.setClientLibraryUrl();//TODO setup
    }

    @Bean
    public WebSocketHandler myHandler() {
        return new WSExeoHandler(redis, listenerContainer, applicationContext);
    }

}