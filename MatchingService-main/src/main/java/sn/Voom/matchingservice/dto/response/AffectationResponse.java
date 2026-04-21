package sn.Voom.matchingservice.dto.response;

import lombok.Builder;
import lombok.Data;
import sn.Voom.matchingservice.entite.enums.StatutAffectation;

import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class AffectationResponse {
    private String id;
    private String trajetId;
    private String conducteurId;
    private List<String> demandesIds;
    private float revenusEstimes;
    private float distanceTotaleKm;
    private LocalDateTime dateAffectation;
    private LocalDateTime dateExpiration;
    private StatutAffectation statut;
    private int tentativeNumero;
    private List<String> conducteursRefusIds;
}
