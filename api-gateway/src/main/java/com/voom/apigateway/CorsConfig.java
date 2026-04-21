package com.voom.apigateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // Indispensable pour SockJS
        config.addAllowedOriginPattern("*"); // Accepte Live Server, localhost, etc.
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // On applique cette règle absolue sur TOUTES les routes de la Gateway
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}