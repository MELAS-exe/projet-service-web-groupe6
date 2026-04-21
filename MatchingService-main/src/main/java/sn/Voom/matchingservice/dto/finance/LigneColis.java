package sn.Voom.matchingservice.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneColis {

    private String bagageId;
    private String expediteurId;
    private String destinataireId;
    private float  poidsKg;
    private float  prixBase;
    private float  supplementPoids;
    private float  montant;         // prixBase + supplementPoids
    private String statut;          // EN_ATTENTE, LIVRE, etc.
    private String typePaiement;
}