package sn.Voom.matchingservice.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Résumé financier complet d'un trajet terminé.
 * Exposé via GET /api/v1/trajets/{id}/resume-financier
 * Consommé par le service financier pour déclencher les paiements.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeFinancier {

    // ── Identifiants ──────────────────────────────────────────────────────────
    private String        trajetId;
    private String        conducteurId;
    private String        vehiculeId;

    // ── Infos trajet ──────────────────────────────────────────────────────────
    private String        villeDepart;
    private String        villeArrivee;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private float         distanceKm;

    // ── Détail passagers ──────────────────────────────────────────────────────
    private List<LignePassager> lignesPassagers;

    // ── Détail colis ──────────────────────────────────────────────────────────
    private List<LigneColis>    lignesColis;

    // ── Totaux ────────────────────────────────────────────────────────────────
    private float totalPassagers;      // somme montants passagers
    private float totalColis;          // somme montants colis
    private float totalBrut;           // totalPassagers + totalColis
    private float commissionVoom;      // totalBrut × tauxCommission
    private float montantConducteur;   // totalBrut - commissionVoom

    // ── Statut ────────────────────────────────────────────────────────────────
    private String statutTrajet;       // doit être TERMINEE pour être valide
}