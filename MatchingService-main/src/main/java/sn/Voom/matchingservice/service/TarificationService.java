package sn.Voom.matchingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sn.Voom.matchingservice.configuration.TarificationProperties;
import sn.Voom.matchingservice.dto.finance.*;

import sn.Voom.matchingservice.entite.DemandeEnvoi;
import sn.Voom.matchingservice.entite.DemandeCourse;
import sn.Voom.matchingservice.entite.Trajet;
import sn.Voom.matchingservice.entite.enums.RegionSenegal;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ════════════════════════════════════════════════════════════════════════════
 * SERVICE DE TARIFICATION VOOM — SÉNÉGAL
 * ════════════════════════════════════════════════════════════════════════════
 *
 * VOYAGE (par place) :
 *   prix = P_base(région_A, région_B)           ← matrice
 *        + C_bagage                              ← grille bagages
 *        + C_distance_hors_centre               ← surcharge GPS
 *   total = prix_par_place × nombrePlaces
 *
 * LIVRAISON (par colis) :
 *   prix = P_base_L(région_A, région_B)         ← matrice livraison
 *        + C_bagage_L(categorieColis)            ← grille livraison
 *        + C_distance_hors_centre_L              ← surcharge GPS
 *
 * Bagages voyage gratuits : 1 mini ou petit par passager.
 * Bagages livraison       : aucun gratuit.
 *
 * Régions : choisies explicitement par l'utilisateur → 100% fiable.
 * Surcharge hors-centre   : calculée automatiquement via GPS.
 * ════════════════════════════════════════════════════════════════════════════
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TarificationService {

    private final TarificationProperties props;
    private final MatriceTarifs          matrice;
    private final RegionDetectionService regionDetection;

    // ── Calcul prix passager ──────────────────────────────────────────────────

    public DetailTarif calculerTarifPassager(CalculTarifRequest req) {
        RegionSenegal rA = req.getRegionDepart();
        RegionSenegal rB = req.getRegionArrivee();

        // 1. Prix de base depuis la matrice
        int prixBase = matrice.getPrixVoyage(rA, rB);
        if (prixBase < 0) {
            log.warn("[Tarif] Trajet non trouvé dans la matrice : {} → {}", rA, rB);
            prixBase = 0;
        }

        // 2. Surcharge hors-centre (calculée automatiquement via GPS)
        double distDepart  = regionDetection.distanceAuCentre(req.getLatDepart(),  req.getLonDepart(),  rA);
        double distArrivee = regionDetection.distanceAuCentre(req.getLatArrivee(), req.getLonArrivee(), rB);
        int surcharge = (int) Math.round((distDepart + distArrivee) * props.getAlphaHcVoyage());

        // 3. Coût bagages voyage (mini et petit = 0 F, moyen = 1 000 F, grand = 2 000 F)
        int coutBagages = req.getQMoyen() * (int) props.getPMoyenVoyage()
                + req.getQGrand() * (int) props.getPGrandVoyage();

        // 4. Total
        int sousTotal     = prixBase + surcharge + coutBagages;
        int totalPassager = sousTotal * req.getNombrePlaces();

        log.info("[Tarif] {} → {} | base={} surcharge={} bagages={} × {}p = {} FCFA",
                rA.nom, rB.nom, prixBase, surcharge, coutBagages,
                req.getNombrePlaces(), totalPassager);

        return DetailTarif.builder()
                .regionDepart(rA.nom).regionArrivee(rB.nom)
                .prixBase(prixBase).surchargeHorsCentre(surcharge)
                .coutBagages(coutBagages).sousTotal(sousTotal)
                .nombrePlaces(req.getNombrePlaces()).totalPassager(totalPassager)
                .distanceKm(matrice.getDistance(rA, rB))
                .distanceDepartCentreKm(distDepart)
                .distanceArriveeCentreKm(distArrivee)
                .build();
    }

    /**
     * Calcule le tarif directement depuis une DemandeCourse.
     * Utilise regionDepart/regionArrivee stockées dans la demande.
     */
    public DetailTarif calculerTarifDepuisDemande(DemandeCourse d) {
        // Régions choisies explicitement par le passager → 100% fiable
        RegionSenegal rA = d.getRegionDepart();
        RegionSenegal rB = d.getRegionArrivee();

        // Catégoriser les bagages depuis le poids
        int[] categories = categorizerBagages(d.getPoidsEstimeBagagesKg(), d.getNombreBagages());

        return calculerTarifPassager(CalculTarifRequest.builder()
                .latDepart(d.getCoordonneesDepart().getLatitude())
                .lonDepart(d.getCoordonneesDepart().getLongitude())
                .latArrivee(d.getCoordonneesArrivee().getLatitude())
                .lonArrivee(d.getCoordonneesArrivee().getLongitude())
                .regionDepart(rA).regionArrivee(rB)
                .nombrePlaces(d.getNombrePlaces())
                .qMini(categories[0]).qPetit(categories[1])
                .qMoyen(categories[2]).qGrand(categories[3])
                .build());
    }

    // ── Calcul prix colis ─────────────────────────────────────────────────────

    /**
     * Calcule le prix d'un colis depuis une DemandeEnvoi.
     * Utilise categorieColis pour la grille tarifaire livraison.
     */
    public int calculerTarifColis(DemandeEnvoi envoi) {
        // ✅ Régions choisies explicitement par l'expéditeur → 100% fiable
        RegionSenegal rA = envoi.getRegionDepart();
        RegionSenegal rB = envoi.getRegionArrivee();

        // 1. Prix de base livraison
        int prixBase = matrice.getPrixLivraison(rA, rB);
        if (prixBase < 0) {
            log.warn("[Tarif-Colis] Trajet non trouvé : {} → {}", rA, rB);
            prixBase = 0;
        }

        // 2. Surcharge hors-centre livraison
        double distDepart  = regionDetection.distanceAuCentre(
                envoi.getCoordonneesDepart().getLatitude(),
                envoi.getCoordonneesDepart().getLongitude(), rA);
        double distArrivee = regionDetection.distanceAuCentre(
                envoi.getCoordonneesArrivee().getLatitude(),
                envoi.getCoordonneesArrivee().getLongitude(), rB);
        int surcharge = (int) Math.round((distDepart + distArrivee) * props.getAlphaHcLivraison());

        // 3. ✅ Coût bagage livraison via categorieColis (aucun gratuit)
        int coutBagage = switch (envoi.getCategorieColis()) {
            case MINI  -> (int) props.getPMiniLivraison();   // 500 FCFA
            case PETIT -> (int) props.getPPetitLivraison();  // 1 000 FCFA
            case MOYEN -> (int) props.getPMoyenLivraison();  // 2 000 FCFA
            case GRAND -> (int) props.getPGrandLivraison();  // 3 500 FCFA
        };

        int total = prixBase + surcharge + coutBagage;
        log.info("[Tarif-Colis] {} → {} | base={} surcharge={} bagage={} = {} FCFA",
                rA.nom, rB.nom, prixBase, surcharge, coutBagage, total);
        return total;
    }

    // ── Résumé financier complet d'un trajet ──────────────────────────────────

    public ResumeFinancier calculerResumeTrajet(Trajet trajet,
                                                List<DemandeCourse> passagers,
                                                List<DemandeEnvoi> colis) {
        List<LignePassager> lignesPassagers = passagers.stream().map(d -> {
            DetailTarif detail = calculerTarifDepuisDemande(d);
            return LignePassager.builder()
                    .demandeId(d.getId().toString())
                    .passagerId(d.getPassagerId())
                    .typePaiement(d.getTypePaiement().name())
                    .nombrePlaces(d.getNombrePlaces())
                    .poidsKgBagage(d.getPoidsEstimeBagagesKg())
                    .prixBase(detail.getPrixBase())
                    .supplementBagage(detail.getCoutBagages())
                    .montant(detail.getTotalPassager())
                    .build();
        }).collect(Collectors.toList());

        List<LigneColis> lignesColis = colis.stream().map(c -> {
            int montant = calculerTarifColis(c);
            return LigneColis.builder()
                    .bagageId(c.getId().toString())
                    .expediteurId(c.getExpediteurId())
                    .destinataireId(c.getDestinataireId())
                    .poidsKg(c.getPoidsKg())
                    .montant(montant)
                    .statut(c.getStatut().name())
                    .build();
        }).collect(Collectors.toList());

        float totalPassagers = (float) lignesPassagers.stream()
                .mapToDouble(LignePassager::getMontant).sum();
        float totalColis     = (float) lignesColis.stream()
                .mapToDouble(LigneColis::getMontant).sum();
        float totalBrut      = totalPassagers + totalColis;
        float commission     = totalBrut * (float) props.getTauxCommission();

        return ResumeFinancier.builder()
                .trajetId(trajet.getId().toString())
                .conducteurId(trajet.getConducteurId())
                .vehiculeId(trajet.getVehiculeId())
                .villeDepart(trajet.getVilleDepart())
                .villeArrivee(trajet.getVilleArrivee())
                .dateDebut(trajet.getDateHeureDepart())
                .dateFin(trajet.getDateHeureArrivee())
                .distanceKm(trajet.getDistanceReelleKm())
                .lignesPassagers(lignesPassagers)
                .lignesColis(lignesColis)
                .totalPassagers(totalPassagers)
                .totalColis(totalColis)
                .totalBrut(totalBrut)
                .commissionVoom(commission)
                .montantConducteur(totalBrut - commission)
                .statutTrajet(trajet.getStatut().name())
                .build();
    }

    // ── Utilitaire : catégoriser bagages depuis le poids ─────────────────────

    private int[] categorizerBagages(float poidsKg, int nombreBagages) {
        if (nombreBagages == 0 || poidsKg == 0) return new int[]{0, 0, 0, 0};
        float poidsMoyen = poidsKg / nombreBagages;
        if (poidsMoyen <= 5)  return new int[]{nombreBagages, 0, 0, 0};
        if (poidsMoyen <= 15) return new int[]{0, nombreBagages, 0, 0};
        if (poidsMoyen <= 30) return new int[]{0, 0, nombreBagages, 0};
        return new int[]{0, 0, 0, nombreBagages};
    }
}