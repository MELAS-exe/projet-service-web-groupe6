package com.voom.iamservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank
        @Pattern(regexp = "^\\+?[0-9\\-\\s]+$", message = "Invalid phone number format")
        String phoneNumber,

        @NotBlank
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) {}
