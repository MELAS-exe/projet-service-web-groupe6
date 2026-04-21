package sn.Voom.matchingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sn.Voom.matchingservice.configuration.MatchingProperties;
import sn.Voom.matchingservice.entite.*;
import sn.Voom.matchingservice.entite.enums.StatutAffectation;
import sn.Voom.matchingservice.entite.enums.StatutDemande;
import sn.Voom.matchingservice.entite.enums.StatutDemandeEnvoi;
import sn.Voom.matchingservice.entite.enums.StatutTrajet;
import sn.Voom.matchingservice.repository.AffectationRepository;
import sn.Voom.matchingservice.repository.DemandeEnvoiRepository;
import sn.Voom.matchingservice.repository.DemandeCourseRepository;
import sn.Voom.matchingservice.repository.TrajetRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final MatchingProperties      props;
    private final TarificationService     tarificationService;
    private final DemandeCourseRepository demandeRepo;
    private final DemandeEnvoiRepository  envoiRepo;
    private final TrajetRepository        trajetRepo;
    private final AffectationRepository   affectationRepo;

    public List<Trajet> executerMatching(List<DemandeCourse> demandes,
                                         int capacitePlaces,
                                         float capaciteChargementKg) {
        if (demandes == null || demandes.isEmpty()) return Collections.emptyList();

        log.info("[Matching] Debut : {} demandes | places={} chargement={}kg",
                demandes.size(), capacitePlaces, capaciteChargementKg);

        int n = demandes.size();
        double[][] scores = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++) {
                double s = scorerPaire(demandes.get(i), demandes.get(j));
                scores[i][j] = s;
                scores[j][i] = s;
            }

        final double SEUIL = 0.4;
        int[] densite = new int[n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (i != j && scores[i][j] >= SEUIL) densite[i]++;

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < n; i++) indices.add(i);
        indices.sort((a, b) -> Integer.compare(densite[b], densite[a]));

        boolean[] assigne = new boolean[n];
        List<Trajet> trajets = new ArrayList<>();

        for (int seedIdx : indices) {
            if (assigne[seedIdx]) continue;

            DemandeCourse seed = demandes.get(seedIdx);
            List<DemandeCourse> groupe = new ArrayList<>();
            groupe.add(seed);
            assigne[seedIdx] = true;

            int placesRestantes  = capacitePlaces - seed.getNombrePlaces();
            float poidsRestantKg = capaciteChargementKg - seed.getPoidsEstimeBagagesKg();

            int[] si = {seedIdx};
            List<Integer> candidats = indices.stream()
                    .filter(j -> !assigne[j] && scores[si[0]][j] >= SEUIL)
                    .sorted((a, b) -> Double.compare(scores[si[0]][b], scores[si[0]][a]))
                    .collect(Collectors.toList());

            for (int candidatIdx : candidats) {
                DemandeCourse candidat = demandes.get(candidatIdx);
                if (candidat.getNombrePlaces() <= placesRestantes
                        && candidat.getPoidsEstimeBagagesKg() <= poidsRestantKg) {
                    groupe.add(candidat);
                    assigne[candidatIdx] = true;
                    placesRestantes -= candidat.getNombrePlaces();
                    poidsRestantKg  -= candidat.getPoidsEstimeBagagesKg();
                }
                if (placesRestantes == 0) break;
            }

            Trajet trajet = creerTrajetDepuisGroupe(groupe, capacitePlaces, capaciteChargementKg);
            trajets.add(trajet);
            log.debug("[Matching] Groupe : {} demandes ({} places) | trajetId={}",
                    groupe.size(),
                    groupe.stream().mapToInt(DemandeCourse::getNombrePlaces).sum(),
                    trajet.getId());
        }

        log.info("[Matching] Termine : {} groupes formes", trajets.size());
        return trajets;
    }

    public boolean tenterAutoIntegration(DemandeCourse nouvelleDemande,
                                         int capacitePlaces,
                                         float capaciteChargementKg) {
        List<Trajet> compatibles = trajetRepo.trouverParStatut(StatutTrajet.EN_ATTENTE_CONDUCTEUR);

        for (Trajet trajet : compatibles) {
            if (!trajet.getVilleDepart().equalsIgnoreCase(nouvelleDemande.getVilleDepart())
                    || !trajet.getVilleArrivee().equalsIgnoreCase(nouvelleDemande.getVilleArrivee())
                    || trajet.getTypeCourse() != nouvelleDemande.getTypeCourse()) continue;

            int   placesDisponibles = trajet.getPlacesTotales() - trajet.getPlacesOccupees();
            float poidsDisponible   = trajet.getCapaciteChargementTotaleKg()
                    - trajet.getCapaciteChargementUtiliseeKg();

            if (nouvelleDemande.getNombrePlaces()        > placesDisponibles) continue;
            if (nouvelleDemande.getPoidsEstimeBagagesKg() > poidsDisponible)  continue;

            long ecartMin = Math.abs(
                    nouvelleDemande.getDateHeureDepart().toEpochSecond(ZoneOffset.UTC)
                            - trajet.getDateHeureDepart().toEpochSecond(ZoneOffset.UTC)) / 60;

            if (ecartMin > nouvelleDemande.getToleranceMinutes()) continue;

            integrerDansTrajetExistant(nouvelleDemande, trajet);
            log.info("[Auto-Integration] Demande {} ({} place(s)) -> trajet {}",
                    nouvelleDemande.getId(), nouvelleDemande.getNombrePlaces(), trajet.getId());
            return true;
        }

        log.debug("[Auto-Integration] Pas de trajet compatible pour demande {}",
                nouvelleDemande.getId());
        return false;
    }

    public Trajet recupererApreRefusOuExpiration(String affectationId,
                                                 String conducteurRefusId,
                                                 StatutAffectation statutFinal,
                                                 int capacitePlaces,
                                                 float capaciteChargement) {
        Affectation ancienne = affectationRepo.trouverParId(affectationId)
                .orElseThrow(() -> new sn.Voom.matchingservice.exception
                        .AffectationNotFoundException(affectationId));

        affectationRepo.mettreAJourStatut(affectationId, statutFinal);
        if (conducteurRefusId != null)
            affectationRepo.ajouterConducteurRefus(affectationId, conducteurRefusId);

        List<DemandeCourse> demandes = demandeRepo.trouverParIds(ancienne.getDemandesIds());
        for (DemandeCourse d : demandes) {
            if (conducteurRefusId != null)
                demandeRepo.ajouterConducteurRefus(d.getId().toString(), conducteurRefusId);
            demandeRepo.mettreAJourStatutEtAffectation(
                    d.getId(), StatutDemande.EN_ATTENTE, (UUID) null, (UUID) null);
            d.setStatut(StatutDemande.EN_ATTENTE);
            d.setAffectationId(null);
        }

        trajetRepo.trouverParId(ancienne.getTrajetId().toString()).ifPresent(ancienTrajet -> {
            if (ancienTrajet.getColisIds() != null)
                ancienTrajet.getColisIds().forEach(colisId ->
                        envoiRepo.mettreAJourStatut(colisId, StatutDemandeEnvoi.EN_ATTENTE));
        });

        trajetRepo.mettreAJourStatut(
                ancienne.getTrajetId().toString(), StatutTrajet.EN_ATTENTE_CONDUCTEUR);

        List<Trajet> nouveaux = executerMatching(demandes, capacitePlaces, capaciteChargement);
        if (nouveaux.isEmpty()) {
            log.warn("[Recuperation] Aucun groupe forme pour affectation {}", affectationId);
            return null;
        }

        Trajet trajet = nouveaux.get(0);
        log.info("[Recuperation] {} -> nouveau trajet {}", affectationId, trajet.getId());

        List<Affectation> nouvAff = affectationRepo.trouverParTrajet(trajet.getId().toString());
        if (!nouvAff.isEmpty()) {
            Affectation nouv = nouvAff.get(0);
            nouv.setTentativeNumero(ancienne.getTentativeNumero() + 1);
            List<String> blacklist = new ArrayList<>(
                    ancienne.getConducteursRefusIds() != null
                            ? ancienne.getConducteursRefusIds() : Collections.emptyList());
            if (conducteurRefusId != null && !blacklist.contains(conducteurRefusId))
                blacklist.add(conducteurRefusId);
            nouv.setConducteursRefusIds(blacklist);
            affectationRepo.sauvegarder(nouv);
        }

        return trajet;
    }

    public double scorerPaire(DemandeCourse a, DemandeCourse b) {
        double dist = haversineKm(
                a.getCoordonneesDepart().getLatitude(), a.getCoordonneesDepart().getLongitude(),
                b.getCoordonneesDepart().getLatitude(), b.getCoordonneesDepart().getLongitude());
        double scoreGeo = Math.max(0, 1.0 - dist / props.getRayonKmDefaut());

        long ecartMin = Math.abs(
                a.getDateHeureDepart().toEpochSecond(ZoneOffset.UTC)
                        - b.getDateHeureDepart().toEpochSecond(ZoneOffset.UTC)) / 60L;
        double fenetre    = Math.max(1, a.getToleranceMinutes() + b.getToleranceMinutes());
        double scoreTemps = Math.max(0, 1.0 - (double) ecartMin / fenetre);

        double poidsCumule = a.getPoidsEstimeBagagesKg() + b.getPoidsEstimeBagagesKg();
        double scoreBagage = poidsCumule <= 100 ? 1.0
                : Math.max(0, 1.0 - (poidsCumule - 100) / 100.0);

        double scoreType = a.getTypeCourse() == b.getTypeCourse() ? 1.0 : 0.0;

        return props.getPoidsGeo()    * scoreGeo
                + props.getPoidsTemps()  * scoreTemps
                + props.getPoidsBagage() * scoreBagage
                + props.getPoidsType()   * scoreType;
    }

    private Trajet creerTrajetDepuisGroupe(List<DemandeCourse> groupe,
                                           int capacitePlaces,
                                           float capaciteChargementKg) {
        double latMoy = groupe.stream()
                .mapToDouble(d -> d.getCoordonneesDepart().getLatitude()).average().orElse(0);
        double lonMoy = groupe.stream()
                .mapToDouble(d -> d.getCoordonneesDepart().getLongitude()).average().orElse(0);
        long epochMoy = (long) groupe.stream()
                .mapToLong(d -> d.getDateHeureDepart().toEpochSecond(ZoneOffset.UTC))
                .average().orElse(0);
        LocalDateTime heureMoy = LocalDateTime.ofEpochSecond(epochMoy, 0, ZoneOffset.UTC);

        int   placesOccupees = groupe.stream().mapToInt(DemandeCourse::getNombrePlaces).sum();
        float poidsKg        = (float) groupe.stream()
                .mapToDouble(DemandeCourse::getPoidsEstimeBagagesKg).sum();

        //  prixCalcule — prix fixé par le système via la matrice tarifaire
        float tarifTotal = (float) groupe.stream()
                .mapToDouble(DemandeCourse::getPrixCalcule).sum();

        DemandeCourse seed = groupe.get(0);
        List<String> demandesIds = groupe.stream()
                .map(d -> d.getId().toString())
                .collect(Collectors.toCollection(ArrayList::new));

        Trajet trajet = Trajet.builder()
                .typeCourse(seed.getTypeCourse())
                .villeDepart(seed.getVilleDepart())
                .villeArrivee(seed.getVilleArrivee())
                .dateHeureDepart(heureMoy)
                .placesOccupees(placesOccupees)
                .placesTotales(capacitePlaces)
                .capaciteChargementUtiliseeKg(poidsKg)
                .capaciteChargementTotaleKg(capaciteChargementKg)
                .tarifTotal(tarifTotal)
                .statut(StatutTrajet.EN_ATTENTE_CONDUCTEUR)
                .demandesIds(demandesIds)
                .colisIds(new ArrayList<>())
                .coordsActuelles(DemandeCourse.toPoint(latMoy, lonMoy))
                .reservationAmisAutorisee(true)
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();

        trajetRepo.sauvegarder(trajet);

        Affectation affectation = Affectation.builder()
                .trajetId(trajet.getId())
                .conducteurId(null)
                .demandesIds(new ArrayList<>(demandesIds))
                .revenusEstimes(tarifTotal)
                .distanceTotaleKm(0f)
                .dateAffectation(LocalDateTime.now())
                .dateExpiration(LocalDateTime.now()
                        .plusMinutes(props.getExpirationAffectationMinutes()))
                .statut(StatutAffectation.PROPOSEE)
                .tentativeNumero(1)
                .conducteursRefusIds(new ArrayList<>())
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();

        affectationRepo.sauvegarder(affectation);

        trajet.setAffectationId(affectation.getId());
        trajetRepo.sauvegarder(trajet);

        for (DemandeCourse d : groupe) {
            demandeRepo.mettreAJourStatutEtAffectation(
                    d.getId(), StatutDemande.AFFECTEE,
                    affectation.getId(), trajet.getId());
        }

        integrerColisCompatibles(trajet);
        return trajet;
    }

    private void integrerColisCompatibles(Trajet trajet) {
        LocalDateTime debut = trajet.getDateHeureDepart().minusHours(2);
        LocalDateTime fin   = trajet.getDateHeureDepart().plusHours(2);

        List<DemandeEnvoi> candidats = envoiRepo.trouverCandidatesMatching(
                trajet.getVilleDepart(), trajet.getVilleArrivee(), debut, fin);

        for (DemandeEnvoi colis : candidats) {
            float poidsDisponible = trajet.getCapaciteChargementTotaleKg()
                    - trajet.getCapaciteChargementUtiliseeKg();

            if (colis.getPoidsKg() > poidsDisponible) {
                log.debug("[Colis] {} refuse — {}kg > {}kg dispo",
                        colis.getId(), colis.getPoidsKg(), poidsDisponible);
                continue;
            }

            trajet.setCapaciteChargementUtiliseeKg(
                    trajet.getCapaciteChargementUtiliseeKg() + colis.getPoidsKg());
            trajet.getColisIds().add(colis.getId().toString());

            //  TarificationService avec matrice tarifaire senegalaise
            int prixFinal = tarificationService.calculerTarifColis(colis);
            envoiRepo.mettreAJourPrixFinal(colis.getId().toString(), prixFinal);
            envoiRepo.mettreAJourStatutEtTrajet(
                    colis.getId().toString(),
                    StatutDemandeEnvoi.AFFECTEE,
                    trajet.getId().toString(),
                    trajet.getAffectationId().toString());

            log.info("[Colis] {} -> trajet {} | {}kg | {} FCFA",
                    colis.getId(), trajet.getId(), colis.getPoidsKg(), prixFinal);
        }

        if (!trajet.getColisIds().isEmpty()) {
            trajet.setDateModification(LocalDateTime.now());
            trajetRepo.sauvegarder(trajet);
        }
    }

    private void integrerDansTrajetExistant(DemandeCourse demande, Trajet trajet) {
        trajet.setPlacesOccupees(trajet.getPlacesOccupees() + demande.getNombrePlaces());
        trajet.setCapaciteChargementUtiliseeKg(
                trajet.getCapaciteChargementUtiliseeKg() + demande.getPoidsEstimeBagagesKg());
        //  prixCalcule au lieu de prixPropose
        trajet.setTarifTotal(trajet.getTarifTotal() + demande.getPrixCalcule());
        trajet.getDemandesIds().add(demande.getId().toString());
        trajet.setDateModification(LocalDateTime.now());
        trajetRepo.sauvegarder(trajet);

        if (trajet.getAffectationId() != null) {
            affectationRepo.trouverParId(trajet.getAffectationId().toString()).ifPresent(aff -> {
                aff.getDemandesIds().add(demande.getId().toString());
                // prixCalcule au lieu
                aff.setRevenusEstimes(aff.getRevenusEstimes() + demande.getPrixCalcule());
                affectationRepo.sauvegarder(aff);
            });
        }

        demandeRepo.mettreAJourStatutEtAffectation(
                demande.getId(), StatutDemande.AFFECTEE,
                trajet.getAffectationId(), trajet.getId());
    }

    public double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}