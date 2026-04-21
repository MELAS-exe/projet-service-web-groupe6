package sn.Voom.matchingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.Voom.matchingservice.entite.Coordonnees;
import sn.Voom.matchingservice.entite.enums.StatutDemande;
import sn.Voom.matchingservice.entite.enums.TypeCourse;
import sn.Voom.matchingservice.entite.enums.TypePaiement;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeCourseResponse {

    private String        id;
    private String        passagerId;
    private TypeCourse    typeCourse;
    private TypePaiement  typePaiement;

    // ── Localisation ──────────────────────────────────────────────────────────
    private String        villeDepart;
    private String        villeArrivee;
    private String        regionDepart;    // nom de la région (ex: "Dakar")
    private String        regionArrivee;   // nom de la région (ex: "Kaolack")
    private Coordonnees   coordonneesDepart;
    private Coordonnees   coordonneesArrivee;

    // ── Horaire ───────────────────────────────────────────────────────────────
    private LocalDateTime dateHeureDepart;
    private int           toleranceMinutes;

    // ── Capacité ──────────────────────────────────────────────────────────────
    private int           nombrePlaces;
    private int           nombreBagages;
    private float         poidsEstimeBagagesKg;
    private List<String>  bagagesIds;

    // ── Prix calculé par le système ───────────────────────────────────────────
    private int           prixCalcule;     // FCFA — calculé depuis la matrice tarifaire

    // ── Statut & Relations ────────────────────────────────────────────────────
    private StatutDemande statut;
    private String        affectationId;
    private String        trajetId;
    private LocalDateTime dateCreation;
}