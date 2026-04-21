package sn.Voom.matchingservice.dto.finance;

import lombok.Builder;
import lombok.Data;
import sn.Voom.matchingservice.entite.enums.RegionSenegal;

/**
 * Détail complet du calcul de tarif exposé dans les réponses.
 */
@Data
@Builder
public class DetailTarif {

    // Régions détectées
    private String regionDepart;
    private String regionArrivee;

    // Composantes du prix
    private int    prixBase;           // P_base de la matrice
    private int    surchargeHorsCentre;// C_distance (hors-centre)
    private int    coutBagages;        // C_bagage
    private int    sousTotal;          // par place
    private int    nombrePlaces;
    private int    totalPassager;      // sousTotal × nombrePlaces

    // Distance routière inter-régions
    private int    distanceKm;

    // Distances hors-centre (km)
    private double distanceDepartCentreKm;
    private double distanceArriveeCentreKm;
}