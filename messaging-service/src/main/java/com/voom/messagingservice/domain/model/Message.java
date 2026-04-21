package com.voom.messagingservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String id;
    private String expediteurId;
    private String destinataireId;
    private String trajetId;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private Boolean lu;

}
