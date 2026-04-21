package sn.Voom.matchingservice.entite;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Enregistre le débit de tokens/crédits d'un conducteur pour un trajet.
 * Collection Firestore : "transactions_credit"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCredit {

    public static final String COLLECTION = "transactions_credit";

    private String id;
    private String conducteurId;
    private String trajetId;

    private float creditsDebites;
    private float soldeAvant;
    private float soldeApres;

    private LocalDateTime dateTransaction;
    private String motif;

    /** DEBITEE, ANNULEE, REMBOURSEE */
    private String statut;
}
