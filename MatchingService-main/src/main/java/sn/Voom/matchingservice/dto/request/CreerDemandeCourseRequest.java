package sn.Voom.matchingservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import sn.Voom.matchingservice.entite.Coordonnees;
import sn.Voom.matchingservice.entite.enums.RegionSenegal;
import sn.Voom.matchingservice.entite.enums.TypeCourse;
import sn.Voom.matchingservice.entite.enums.TypePaiement;

import java.time.LocalDateTime;

@Data
public class CreerDemandeCourseRequest {

    @NotBlank(message = "L'identifiant du passager est obligatoire")
    private String passagerId;

    @NotNull(message = "Le type de course est obligatoire")
    private TypeCourse typeCourse;

    @NotNull(message = "Le type de paiement est obligatoire")
    private TypePaiement typePaiement;

    @NotBlank(message = "La ville de départ est obligatoire")
    private String villeDepart;

    @NotBlank(message = "La ville d'arrivée est obligatoire")
    private String villeArrivee;

    //  Régions explicitement choisies — 100% fiable pour la tarification
    @NotNull(message = "La région de départ est obligatoire")
    private RegionSenegal regionDepart;

    @NotNull(message = "La région d'arrivée est obligatoire")
    private RegionSenegal regionArrivee;

    @NotNull @Valid
    private CoordonneesRequest coordonneesDepart;

    @NotNull @Valid
    private CoordonneesRequest coordonneesArrivee;

    @NotNull(message = "La date/heure de départ est obligatoire")
    @Future(message = "La date de départ doit être dans le futur")
    private LocalDateTime dateHeureDepart;

    @Min(0) @Max(120)
    private int toleranceMinutes = 15;

    @Min(1) @Max(4)
    private int nombrePlaces = 1;

    @Min(0)
    private int nombreBagages = 0;

    @DecimalMin("0.0")
    private float poidsEstimeBagagesKg = 0f;

    private String descriptionBagages;

    // ✅ prixPropose supprimé — prix fixé par le système via la matrice tarifaire

    private String invitationTrajetId;

    // ── DTO interne coordonnées ───────────────────────────────────────────────
    @Data
    public static class CoordonneesRequest {

        private static final GeometryFactory GEO = new GeometryFactory(new PrecisionModel(), 4326);

        @NotNull
        @DecimalMin("-90.0") @DecimalMax("90.0")
        private Double latitude;

        @NotNull
        @DecimalMin("-180.0") @DecimalMax("180.0")
        private Double longitude;

        private String adresse;

        public Coordonnees toEntite() {
            return Coordonnees.builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .adresse(adresse)
                    .build();
        }

        public Point toPoint() {
            return GEO.createPoint(new Coordinate(longitude, latitude));
        }
    }
}