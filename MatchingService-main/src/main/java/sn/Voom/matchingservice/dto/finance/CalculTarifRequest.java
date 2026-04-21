package sn.Voom.matchingservice.dto.finance;

import lombok.Builder;
import lombok.Data;
import sn.Voom.matchingservice.entite.enums.RegionSenegal;

/**
 * Paramètres d'entrée pour le calcul de tarif d'un passager.
 */
@Data
@Builder
public class CalculTarifRequest {

    // Coordonnées réelles du passager (pour surcharge hors-centre)
    private double latDepart;
    private double lonDepart;
    private double latArrivee;
    private double lonArrivee;

    // Régions détectées automatiquement
    private RegionSenegal regionDepart;
    private RegionSenegal regionArrivee;

    // Nombre de places
    private int nombrePlaces;

    // Bagages
    private int qMini;    // quantité mini
    private int qPetit;   // quantité petit
    private int qMoyen;   // quantité moyen
    private int qGrand;   // quantité grand
}