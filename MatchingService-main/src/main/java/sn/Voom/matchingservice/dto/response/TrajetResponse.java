package sn.Voom.matchingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrajetResponse {

    // ── Identifiants ──────────────────────────────────────────────────────────
    private String        id;
    private String        conducteurId;    // assigné à l'acceptation
    private String        vehiculeId;      // assigné à l'acceptation

    // ── Infos trajet ──────────────────────────────────────────────────────────
    private String        typeCourse;
    private String        villeDepart;
    private String        villeArrivee;
    private LocalDateTime dateHeureDepart;
    private LocalDateTime dateHeureArrivee;

    // ── Capacité ──────────────────────────────────────────────────────────────
    private int           placesOccupees;
    private int           placesTotales;
    private float         capaciteChargementUtiliseeKg;
    private float         capaciteChargementTotaleKg;

    // ── Finance ───────────────────────────────────────────────────────────────
    private float         tarifTotal;          // estimé (somme prix proposés)
    private float         distanceReelleKm;    // calculée à TERMINEE

    // ── Statut ────────────────────────────────────────────────────────────────
    private String        statut;

    // ── Passagers ─────────────────────────────────────────────────────────────
    private List<String>  demandesIds;
    private String        affectationId;
}