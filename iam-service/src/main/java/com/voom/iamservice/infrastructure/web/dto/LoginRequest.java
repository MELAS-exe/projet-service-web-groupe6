package com.voom.iamservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String phoneNumber,
        @NotBlank String password
) {
}
