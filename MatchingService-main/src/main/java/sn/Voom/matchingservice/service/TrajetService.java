package sn.Voom.matchingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sn.Voom.matchingservice.configuration.TarificationProperties;
import sn.Voom.matchingservice.dto.finance.ResumeFinancier;
import sn.Voom.matchingservice.dto.response.TrajetResponse;
import sn.Voom.matchingservice.entite.Bagage;
import sn.Voom.matchingservice.entite.DemandeCourse;
import sn.Voom.matchingservice.entite.DemandeEnvoi;
import sn.Voom.matchingservice.entite.Trajet;
import sn.Voom.matchingservice.entite.enums.StatutDemande;
import sn.Voom.matchingservice.entite.enums.StatutTrajet;
import sn.Voom.matchingservice.exception.TrajetNotFoundException;
import sn.Voom.matchingservice.repository.BagageRepository;
import sn.Voom.matchingservice.repository.DemandeCourseRepository;
import sn.Voom.matchingservice.repository.DemandeEnvoiRepository;
import sn.Voom.matchingservice.repository.TrajetRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrajetService {

    private final TrajetRepository       trajetRepo;
    private final DemandeCourseRepository demandeRepo;
    private final TarificationService    tarificationService;
    private final DemandeEnvoiRepository envoiRepo;

    // ── Trouver par ID ────────────────────────────────────────────────────────

    public TrajetResponse trouverParId(String id) {
        Trajet trajet = trajetRepo.trouverParId(id)
                .orElseThrow(() -> new TrajetNotFoundException(id));
        return versResponse(trajet);
    }

    // ── Lister par conducteur ─────────────────────────────────────────────────

    public List<TrajetResponse> listerParConducteur(String conducteurId) {
        return trajetRepo.trouverParConducteur(conducteurId)
                .stream().map(this::versResponse).collect(Collectors.toList());
    }

    // ── Lister par statut ─────────────────────────────────────────────────────

    public List<TrajetResponse> listerParStatut(String statut) {
        StatutTrajet s = StatutTrajet.valueOf(statut.toUpperCase());
        return trajetRepo.trouverParStatut(s)
                .stream().map(this::versResponse).collect(Collectors.toList());
    }

    // ── Démarrer un trajet ────────────────────────────────────────────────────

    public TrajetResponse demarrer(String id) {
        Trajet trajet = trajetRepo.trouverParId(id)
                .orElseThrow(() -> new TrajetNotFoundException(id));

        if (trajet.getStatut() != StatutTrajet.PROGRAMME) {
            throw new IllegalStateException(
                    "Impossible de démarrer un trajet en statut " + trajet.getStatut());
        }

        trajet.setStatut(StatutTrajet.EN_COURS);
        trajet.setDateHeureDepart(LocalDateTime.now());
        trajet.setDateModification(LocalDateTime.now());
        trajetRepo.sauvegarder(trajet);

        // Mettre à jour les demandes passagers → EN_COURS
        List<DemandeCourse> demandes = demandeRepo.trouverParIds(trajet.getDemandesIds());
        demandes.forEach(d -> {
            demandeRepo.mettreAJourStatut(d.getId().toString(), StatutDemande.EN_COURS);
        });

        log.info("[Trajet] Démarré : {}", id);
        return versResponse(trajet);
    }

    // ── Terminer un trajet ────────────────────────────────────────────────────

    public TrajetResponse terminer(String id) {
        Trajet trajet = trajetRepo.trouverParId(id)
                .orElseThrow(() -> new TrajetNotFoundException(id));

        if (trajet.getStatut() != StatutTrajet.EN_COURS) {
            throw new IllegalStateException(
                    "Impossible de terminer un trajet en statut " + trajet.getStatut());
        }

        // ── Calculer la distance réelle ───────────────────────────────────────
        // TODO: intégrer Google Maps Distance Matrix API
        // Pour l'instant on utilise la distance Haversine entre départ et arrivée
        float distanceKm = calculerDistanceEstimee(trajet);

        trajet.setStatut(StatutTrajet.TERMINE);
        trajet.setDistanceReelleKm(distanceKm);
        trajet.setDateHeureArrivee(LocalDateTime.now());
        trajet.setDateModification(LocalDateTime.now());
        trajetRepo.sauvegarder(trajet);

        // Mettre à jour les demandes passagers → TERMINEE
        List<DemandeCourse> demandes = demandeRepo.trouverParIds(trajet.getDemandesIds());
        demandes.forEach(d -> {
            demandeRepo.mettreAJourStatut(d.getId().toString(), StatutDemande.TERMINEE);
        });

        log.info("[Trajet] Terminé : {} | distance={}km", id, distanceKm);
        return versResponse(trajet);
    }

    // ── Résumé financier (consommé par le service financier) ──────────────────

    public ResumeFinancier getResumeFinancier(String id) {
        Trajet trajet = trajetRepo.trouverParId(id)
                .orElseThrow(() -> new TrajetNotFoundException(id));

        if (trajet.getStatut() != StatutTrajet.TERMINE) {
            throw new IllegalStateException(
                    "Le résumé financier n'est disponible qu'après la fin du trajet. " +
                            "Statut actuel : " + trajet.getStatut());
        }

        // Récupérer passagers et bagages
        List<DemandeCourse> passagers = demandeRepo.trouverParIds(trajet.getDemandesIds());
        List<DemandeEnvoi>  colis     = trajet.getColisIds() != null && !trajet.getColisIds().isEmpty()
                ? envoiRepo.trouverParIds(trajet.getColisIds())
                : new ArrayList<>();
        return tarificationService.calculerResumeTrajet(trajet, passagers, colis);
    }

    // ── Lister les demandes d'un trajet (pour la finance) ─────────────────────

    public List<DemandeCourse> listerPassagers(String trajetId) {
        Trajet trajet = trajetRepo.trouverParId(trajetId)
                .orElseThrow(() -> new TrajetNotFoundException(trajetId));
        return demandeRepo.trouverParIds(trajet.getDemandesIds());
    }

    // ── Distance estimée (Haversine entre coords de départ et arrivée) ────────

    private float calculerDistanceEstimee(Trajet trajet) {
        // Utilise les coordonnées stockées dans le trajet
        // TODO : remplacer par Google Maps API pour la distance réelle
        if (trajet.getCoordsDepart() == null || trajet.getCoordsArrivee() == null) {
            log.warn("[Trajet] Coordonnées manquantes pour calcul distance trajet {}", trajet.getId());
            return 0f;
        }
        double lat1 = trajet.getCoordsDepart().getLatitude();
        double lon1 = trajet.getCoordsDepart().getLongitude();
        double lat2 = trajet.getCoordsArrivee().getLatitude();
        double lon2 = trajet.getCoordsArrivee().getLongitude();

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return (float) (6371.0 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    // ── Mapper vers Response ──────────────────────────────────────────────────

    private TrajetResponse versResponse(Trajet t) {
        return TrajetResponse.builder()
                .id(t.getId() != null ? t.getId().toString() : null)
                .typeCourse(t.getTypeCourse() != null ? t.getTypeCourse().name() : null)
                .villeDepart(t.getVilleDepart())
                .villeArrivee(t.getVilleArrivee())
                .dateHeureDepart(t.getDateHeureDepart())
                .dateHeureArrivee(t.getDateHeureArrivee())
                .placesOccupees(t.getPlacesOccupees())
                .placesTotales(t.getPlacesTotales())
                .tarifTotal(t.getTarifTotal())
                .distanceReelleKm(t.getDistanceReelleKm())
                .conducteurId(t.getConducteurId())
                .vehiculeId(t.getVehiculeId())
                .statut(t.getStatut() != null ? t.getStatut().name() : null)
                .demandesIds(t.getDemandesIds())
                .build();
    }
}