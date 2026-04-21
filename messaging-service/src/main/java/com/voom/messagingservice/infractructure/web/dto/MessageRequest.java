package com.voom.messagingservice.infractructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record MessageRequest(
        @NotBlank String expediteurId,
        @NotBlank String destinataireId,
        @NotBlank String trajetId,
        @NotBlank String contenu
) {
}
