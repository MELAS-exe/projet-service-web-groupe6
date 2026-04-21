package com.voom.iamservice.infrastructure.web.dto;

public record AuthResponse(
        String token,
        String refreshToken
) {}
