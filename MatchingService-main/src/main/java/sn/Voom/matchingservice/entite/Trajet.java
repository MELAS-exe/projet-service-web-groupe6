package sn.Voom.matchingservice.entite;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;
import sn.Voom.matchingservice.entite.enums.StatutTrajet;
import sn.Voom.matchingservice.entite.enums.TypeCourse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "trajets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Trajet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "conducteur_id")
    private String conducteurId;

    @Column(name = "vehicule_id")
    private String vehiculeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_course", nullable = false)
    private TypeCourse typeCourse;

    @Column(name = "ville_depart", nullable = false)
    private String villeDepart;

    @Column(name = "ville_arrivee", nullable = false)
    private String villeArrivee;

    @Column(name = "date_heure_depart", nullable = false)
    private LocalDateTime dateHeureDepart;

    @Column(name = "date_heure_fin")
    private LocalDateTime dateHeureFin;

    @Column(name = "places_occupees")
    private int placesOccupees = 0;

    @Column(name = "places_totales")
    private int placesTotales = 4;

    @Column(name = "capacite_chargement_utilisee_kg")
    private float capaciteChargementUtiliseeKg = 0f;

    @Column(name = "capacite_chargement_totale_kg")
    private float capaciteChargementTotaleKg = 50f;

    @Column(name = "tarif_total")
    private float tarifTotal = 0f;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutTrajet statut = StatutTrajet.EN_ATTENTE_CONDUCTEUR;

    @Column(name = "affectation_id")
    private UUID affectationId;

    @Column(name = "reservation_amis_autorisee")
    private boolean reservationAmisAutorisee = true;

    @Column(name = "coords_actuelles", columnDefinition = "geography(Point,4326)")
    private Point coordsActuelles;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "demandes_ids", columnDefinition = "text[]")
    private List<String> demandesIds;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @Column(name = "distance_reelle_km")
    private float distanceReelleKm = 0f;

    @Column(name = "date_heure_arrivee")
    private LocalDateTime dateHeureArrivee;

    @Column(name = "coords_depart", columnDefinition = "geography(Point,4326)")
    private Point coordsDepart;

    @Column(name = "coords_arrivee", columnDefinition = "geography(Point,4326)")
    private Point coordsArrivee;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "colis_ids", columnDefinition = "text[]")
    private List<String> colisIds;

    @PrePersist
    void prePersist() {
        if (dateCreation == null)     dateCreation = LocalDateTime.now();
        if (dateModification == null) dateModification = LocalDateTime.now();
        if (demandesIds == null)      demandesIds = new ArrayList<>();
        if (colisIds == null) colisIds  = new ArrayList<>();
    }

    @Transient
    public Coordonnees getCoordonneesActuelles() {
        if (coordsActuelles == null) return null;
        return Coordonnees.builder()
                .latitude(coordsActuelles.getY())
                .longitude(coordsActuelles.getX())
                .build();
    }

    @Transient
    public Coordonnees getCoordsDepart() {
        if (coordsDepart == null) return null;
        return Coordonnees.builder()
                .latitude(coordsDepart.getY())
                .longitude(coordsDepart.getX())
                .build();
    }

    @Transient
    public Coordonnees getCoordsArrivee() {
        if (coordsArrivee == null) return null;
        return Coordonnees.builder()
                .latitude(coordsArrivee.getY())
                .longitude(coordsArrivee.getX())
                .build();
    }

    @Transient
    public List<String> getEtapes() { return new ArrayList<>(); }
}