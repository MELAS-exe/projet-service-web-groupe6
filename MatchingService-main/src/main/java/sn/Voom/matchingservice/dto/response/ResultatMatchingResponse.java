package sn.Voom.matchingservice.dto.response;

import lombok.Builder;
import lombok.Data;
import sn.Voom.matchingservice.entite.enums.StatutInvitation;

import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class ResultatMatchingResponse {
    private int nombreGroupesFormes;
    private int nombreDemandesTraitees;
    private int nombreDemandesEnAttente;
    private List<TrajetResponse> trajets;
    private List<AffectationResponse> affectations;
    private LocalDateTime executéÀ;
}
