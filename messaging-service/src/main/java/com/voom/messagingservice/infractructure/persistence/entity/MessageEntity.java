package com.voom.messagingservice.infractructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "messages")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageEntity {
    @Id
    private String id;
    private String expediteurId;
    private String destinataireId;
    private String trajetId;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private Boolean lu;
}