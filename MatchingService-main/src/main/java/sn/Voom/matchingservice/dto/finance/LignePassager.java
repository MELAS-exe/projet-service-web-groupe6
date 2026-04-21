package sn.Voom.matchingservice.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LignePassager {

    private String demandeId;
    private String passagerId;
    private String typePaiement;    // WAVE, ORANGE_MONEY, CASH
    private int    nombrePlaces;
    private float  poidsKgBagage;
    private float  prixBase;
    private float  supplementBagage;
    private float  montant;         // prixBase + distance + supplementBagage
}