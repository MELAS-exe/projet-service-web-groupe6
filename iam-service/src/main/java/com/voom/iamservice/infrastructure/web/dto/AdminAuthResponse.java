package com.voom.iamservice.infrastructure.web.dto;

import com.voom.iamservice.domain.model.Role;

import java.util.Set;
import java.util.UUID;

public record AdminAuthResponse(

        UUID id,
        String firstName,
        String lastName,
        String phoneNumber,
        String password,
        Set<Role>roles
) {
}
