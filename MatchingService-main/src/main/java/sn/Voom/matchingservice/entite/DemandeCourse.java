package sn.Voom.matchingservice.entite;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import sn.Voom.matchingservice.entite.enums.RegionSenegal;
import sn.Voom.matchingservice.entite.enums.StatutDemande;
import sn.Voom.matchingservice.entite.enums.TypeCourse;
import sn.Voom.matchingservice.entite.enums.TypePaiement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "demandes_course")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeCourse {

    private static final GeometryFactory GEO = new GeometryFactory(new PrecisionModel(), 4326);

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "passager_id", nullable = false)
    private String passagerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_course", nullable = false)  // retiré columnDefinition
    private TypeCourse typeCourse;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_paiement", nullable = false)  // retiré columnDefinition
    private TypePaiement typePaiement;

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

    // ── PostGIS : Point(longitude, latitude) ──────────────────────────────────
    @Column(name = "coords_depart", columnDefinition = "geography(Point,4326)")
    private Point coordsDepart;

    @Column(name = "coords_arrivee", columnDefinition = "geography(Point,4326)")
    private Point coordsArrivee;

    @Column(name = "date_heure_depart", nullable = false)
    private LocalDateTime dateHeureDepart;

    @Column(name = "tolerance_minutes")
    private int toleranceMinutes = 15;

    @Column(name = "nombre_places")
    private int nombrePlaces = 1;

    @Column(name = "nombre_bagages")
    private int nombreBagages = 0;

    @Column(name = "poids_estime_bagages_kg")
    private float poidsEstimeBagagesKg = 0f;

    @Column(name = "description_bagages")
    private String descriptionBagages;

    @Column(name = "prix_calcule")
    private int prixCalcule = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    @Column(name = "affectation_id")
    private UUID affectationId;

    @Column(name = "trajet_id")
    private UUID trajetId;

    @Column(name = "invitation_trajet_id")
    private UUID invitationTrajetId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "conducteurs_refus_ids", columnDefinition = "text[]")
    private List<String> conducteursRefusIds;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PrePersist
    void prePersist() {
        if (dateCreation == null)       dateCreation = LocalDateTime.now();
        if (dateModification == null)   dateModification = LocalDateTime.now();
        if (conducteursRefusIds == null) conducteursRefusIds = new ArrayList<>();
    }

    // ── Helper : construire un Point PostGIS depuis lat/lon ───────────────────
    public static Point toPoint(double latitude, double longitude) {
        return GEO.createPoint(new Coordinate(longitude, latitude)); // GeoJSON : lon, lat
    }

    // ── Compatibilité avec l'ancien code Coordonnees ──────────────────────────
    // Ces getters permettent à MatchingService de continuer à fonctionner
    // sans modification
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

    public List<String> getBagagesIds() {
        return new ArrayList<>(); // géré via la relation bagages
    }
}