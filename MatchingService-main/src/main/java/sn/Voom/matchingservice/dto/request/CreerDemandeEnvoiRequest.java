package sn.Voom.matchingservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import sn.Voom.matchingservice.entite.enums.CategorieColis;
import sn.Voom.matchingservice.entite.enums.RegionSenegal;
import sn.Voom.matchingservice.entite.enums.TypePaiement;

import java.time.LocalDateTime;

import static sn.Voom.matchingservice.dto.request.CreerDemandeCourseRequest.CoordonneesRequest;

@Data
public class CreerDemandeEnvoiRequest {

    @NotBlank(message = "L'identifiant de l'expéditeur est obligatoire")
    private String expediteurId;

    @NotBlank(message = "L'identifiant du destinataire est obligatoire")
    private String destinataireId;

    // ── Trajet ────────────────────────────────────────────────────────────────
    @NotBlank(message = "La ville de départ est obligatoire")
    private String villeDepart;

    @NotBlank(message = "La ville d'arrivée est obligatoire")
    private String villeArrivee;

    //  Régions explicitement choisies par l'utilisateur (100% fiable)
    @NotNull(message = "La région de départ est obligatoire")
    private RegionSenegal regionDepart;

    @NotNull(message = "La région d'arrivée est obligatoire")
    private RegionSenegal regionArrivee;

    @NotNull @Valid
    private CoordonneesRequest coordonneesDepart;

    @NotNull @Valid
    private CoordonneesRequest coordonneesArrivee;

    @NotNull(message = "La date/heure souhaitée est obligatoire")
    @Future(message = "La date doit être dans le futur")
    private LocalDateTime dateHeureSouhaitee;

    @Min(0) @Max(240)
    private int toleranceMinutes = 60;

    // ── Colis ─────────────────────────────────────────────────────────────────
    @NotBlank(message = "La description du colis est obligatoire")
    private String description;

    @DecimalMin("0.1")
    @DecimalMax("50.0")
    private float poidsKg;

    // Catégorie choisie par l'utilisateur — détermine le prix
    @NotNull(message = "La catégorie du colis est obligatoire")
    private CategorieColis categorieColis;

    private boolean fragile = false;
    private String  dimensionsCm;

    // ── Paiement ──────────────────────────────────────────────────────────────
    @NotNull(message = "Le type de paiement est obligatoire")
    private TypePaiement typePaiement;
}