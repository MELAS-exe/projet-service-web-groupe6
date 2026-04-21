package sn.Voom.matchingservice.entite;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import sn.Voom.matchingservice.entite.enums.CategorieColis;
import sn.Voom.matchingservice.entite.enums.RegionSenegal;
import sn.Voom.matchingservice.entite.enums.StatutDemandeEnvoi;
import sn.Voom.matchingservice.entite.enums.TypePaiement;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "demandes_envoi")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeEnvoi {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ── Parties prenantes ─────────────────────────────────────────────────────
    @Column(name = "expediteur_id", nullable = false)
    private String expediteurId;

    @Column(name = "destinataire_id", nullable = false)
    private String destinataireId;

    // ── Trajet ────────────────────────────────────────────────────────────────
    @Column(name = "ville_depart", nullable = false)
    private String villeDepart;

    @Column(name = "ville_arrivee", nullable = false)
    private String villeArrivee;

    @Enumerated(EnumType.STRING)
    @Column(name = "region_depart")
    private RegionSenegal regionDepart;

    @Enumerated(EnumType.STRING)
    @Column(name = "region_arrivee")
    private RegionSenegal regionArrivee;

    @Column(name = "adresse_depart")
    private String adresseDepart;

    @Column(name = "adresse_arrivee")
    private String adresseArrivee;

    @Column(name = "coords_depart", columnDefinition = "geography(Point,4326)")
    private Point coordsDepart;

    @Column(name = "coords_arrivee", columnDefinition = "geography(Point,4326)")
    private Point coordsArrivee;

    @Column(name = "date_heure_souhaitee", nullable = false)
    private LocalDateTime dateHeureSouhaitee;

    @Column(name = "tolerance_minutes")
    private int toleranceMinutes = 60;

    // ── Colis ─────────────────────────────────────────────────────────────────
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "poids_kg", nullable = false)
    private float poidsKg;

    // ✅ Catégorie du colis — détermine le prix de livraison
    @Enumerated(EnumType.STRING)
    @Column(name = "categorie_colis", nullable = false)
    private CategorieColis categorieColis;

    @Column(name = "fragile")
    private boolean fragile = false;

    @Column(name = "dimensions_cm")
    private String dimensionsCm;

    // ── Paiement ──────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "type_paiement", nullable = false)
    private TypePaiement typePaiement;

    @Column(name = "prix_final")
    private float prixFinal = 0f;  // calculé automatiquement par le système

    // ── Statut & Relations ────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutDemandeEnvoi statut = StatutDemandeEnvoi.EN_ATTENTE;

    @Column(name = "trajet_id")
    private UUID trajetId;

    @Column(name = "affectation_id")
    private UUID affectationId;

    // ── Confirmation livraison ────────────────────────────────────────────────
    @Column(name = "code_confirmation")
    private String codeConfirmation;

    @Column(name = "date_confirmation_livraison")
    private LocalDateTime dateConfirmationLivraison;

    // ── Dates ─────────────────────────────────────────────────────────────────
    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PrePersist
    void prePersist() {
        if (dateCreation     == null) dateCreation     = LocalDateTime.now();
        if (dateModification == null) dateModification = LocalDateTime.now();
        if (codeConfirmation == null) codeConfirmation = genererCode();
    }

    // ── Helpers coordonnées ───────────────────────────────────────────────────
    @Transient
    public Coordonnees getCoordonneesDepart() {
        if (coordsDepart == null) return null;
        return Coordonnees.builder()
                .latitude(coordsDepart.getY())
                .longitude(coordsDepart.getX())
                .adresse(adresseDepart)
                .build();
    }

    @Transient
    public Coordonnees getCoordonneesArrivee() {
        if (coordsArrivee == null) return null;
        return Coordonnees.builder()
                .latitude(coordsArrivee.getY())
                .longitude(coordsArrivee.getX())
                .adresse(adresseArrivee)
                .build();
    }

    private String genererCode() {
        return String.valueOf((int)(Math.random() * 9000) + 1000);
    }
}