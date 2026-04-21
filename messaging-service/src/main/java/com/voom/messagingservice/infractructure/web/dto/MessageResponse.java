package com.voom.messagingservice.infractructure.web.dto;

import java.time.LocalDateTime;

public record MessageResponse(
        String id,
        String expediteurId,
        String destinataireId,
        String trajetId,
        String contenu,
        LocalDateTime dateEnvoi,
        Boolean lu
) {
}
