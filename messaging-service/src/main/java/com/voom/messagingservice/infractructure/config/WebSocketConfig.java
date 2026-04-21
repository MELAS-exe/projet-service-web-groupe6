package com.voom.messagingservice.infractructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Préfixe pour que les clients reçoivent les messages (ex: /topic/trajet/123)
        config.enableSimpleBroker("/topic", "/queue");
        // Préfixe pour envoyer des messages depuis le client
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint de connexion WebSocket via l'API Gateway
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}