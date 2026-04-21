package sn.Voom.matchingservice.dto.response;

import lombok.Builder;
import lombok.Data;
import sn.Voom.matchingservice.entite.enums.StatutBagage;
import sn.Voom.matchingservice.entite.enums.TypeBagage;

import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class BagageResponse {
    private String id;
    private String demandeId;
    private String expediteurId;
    private String destinataireId;
    private String description;
    private List<TypeBagage> types;
    private float poidsKg;
    private boolean fragile;
    private boolean entreAmis;
    private String amiDestinataireId;
    private StatutBagage statut;
    private LocalDateTime dateCreation;
}
