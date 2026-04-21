package sn.Voom.matchingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.Voom.matchingservice.entite.enums.CategorieColis;
import sn.Voom.matchingservice.entite.enums.StatutDemandeEnvoi;
import sn.Voom.matchingservice.entite.enums.TypePaiement;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeEnvoiResponse {

    private String             id;
    private String             expediteurId;
    private String             destinataireId;
    private String             villeDepart;
    private String             villeArrivee;
    private String             adresseDepart;
    private String             adresseArrivee;
    private String             regionDepart;    // nom de la région détectée
    private String             regionArrivee;   // nom de la région détectée
    private LocalDateTime      dateHeureSouhaitee;
    private int                toleranceMinutes;
    private String             description;
    private float              poidsKg;
    private CategorieColis     categorieColis;  //  catégorie du colis
    private boolean            fragile;
    private String             dimensionsCm;
    private TypePaiement       typePaiement;
    private float              prixFinal;       // calculé par le système
    private StatutDemandeEnvoi statut;
    private String             trajetId;
    private String             codeConfirmation;
    private LocalDateTime      dateCreation;
    private LocalDateTime      dateConfirmationLivraison;
}