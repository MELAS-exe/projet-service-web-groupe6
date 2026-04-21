package sn.Voom.matchingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sn.Voom.matchingservice.dto.request.CreerDemandeEnvoiRequest;
import sn.Voom.matchingservice.dto.response.DemandeEnvoiResponse;
import sn.Voom.matchingservice.entite.DemandeEnvoi;
import sn.Voom.matchingservice.entite.Trajet;
import sn.Voom.matchingservice.entite.enums.StatutDemandeEnvoi;
import sn.Voom.matchingservice.entite.enums.StatutTrajet;
import sn.Voom.matchingservice.exception.DemandeEnvoiNotFoundException;
import sn.Voom.matchingservice.repository.DemandeEnvoiRepository;
import sn.Voom.matchingservice.repository.TrajetRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemandeEnvoiService {

    private final DemandeEnvoiRepository envoiRepo;
    private final TrajetRepository       trajetRepo;
    private final TarificationService    tarificationService;

    // ── Créer une demande d'envoi ─────────────────────────────────────────────

    public DemandeEnvoiResponse creer(CreerDemandeEnvoiRequest req) {
        DemandeEnvoi envoi = DemandeEnvoi.builder()
                .expediteurId(req.getExpediteurId())
                .destinataireId(req.getDestinataireId())
                .villeDepart(req.getVilleDepart())
                .villeArrivee(req.getVilleArrivee())
                .regionDepart(req.getRegionDepart())   // ✅ région explicite
                .regionArrivee(req.getRegionArrivee()) // ✅ région explicite
                .coordsDepart(req.getCoordonneesDepart().toPoint())
                .coordsArrivee(req.getCoordonneesArrivee().toPoint())
                .adresseDepart(req.getCoordonneesDepart().getAdresse())
                .adresseArrivee(req.getCoordonneesArrivee().getAdresse())
                .dateHeureSouhaitee(req.getDateHeureSouhaitee())
                .toleranceMinutes(req.getToleranceMinutes())
                .description(req.getDescription())
                .poidsKg(req.getPoidsKg())
                .categorieColis(req.getCategorieColis()) // ✅ catégorie explicite
                .fragile(req.isFragile())
                .dimensionsCm(req.getDimensionsCm())
                .typePaiement(req.getTypePaiement())
                .statut(StatutDemandeEnvoi.EN_ATTENTE)
                .build();

        envoiRepo.sauvegarder(envoi);
        log.info("[Envoi] Créé : {} | {} | {}kg | {}→{}",
                envoi.getId(), envoi.getCategorieColis(), envoi.getPoidsKg(),
                envoi.getVilleDepart(), envoi.getVilleArrivee());

        // ✅ Calcul automatique du prix via matrice tarifaire
        int prixFinal = tarificationService.calculerTarifColis(envoi);
        envoiRepo.mettreAJourPrixFinal(envoi.getId().toString(), prixFinal);
        log.info("[Envoi] Prix calculé : {} FCFA ({} → {} | {})",
                prixFinal, envoi.getRegionDepart(), envoi.getRegionArrivee(), envoi.getCategorieColis());

        // ── Tenter intégration dans un trajet existant ────────────────────────
        boolean integre = tenterIntegrationDansTrajet(envoi, prixFinal);
        if (!integre) {
            log.info("[Envoi] Aucun trajet compatible — en attente");
        }

        return versResponse(envoiRepo.trouverParId(envoi.getId().toString()).orElse(envoi));
    }

    // ── Intégration dans un trajet existant ───────────────────────────────────

    private boolean tenterIntegrationDansTrajet(DemandeEnvoi envoi, int prixFinal) {
        List<Trajet> trajetsCompatibles = trajetRepo
                .trouverParStatut(StatutTrajet.EN_ATTENTE_CONDUCTEUR);

        for (Trajet trajet : trajetsCompatibles) {
            if (!trajet.getVilleDepart().equalsIgnoreCase(envoi.getVilleDepart())
                    || !trajet.getVilleArrivee().equalsIgnoreCase(envoi.getVilleArrivee())) continue;

            float poidsDisponible = trajet.getCapaciteChargementTotaleKg()
                    - trajet.getCapaciteChargementUtiliseeKg();
            if (envoi.getPoidsKg() > poidsDisponible) continue;

            long ecartMin = Math.abs(
                    envoi.getDateHeureSouhaitee().toEpochSecond(ZoneOffset.UTC)
                            - trajet.getDateHeureDepart().toEpochSecond(ZoneOffset.UTC)) / 60;
            if (ecartMin > envoi.getToleranceMinutes()) continue;

            trajet.setCapaciteChargementUtiliseeKg(
                    trajet.getCapaciteChargementUtiliseeKg() + envoi.getPoidsKg());
            if (trajet.getColisIds() == null) trajet.setColisIds(new java.util.ArrayList<>());
            trajet.getColisIds().add(envoi.getId().toString());
            trajet.setDateModification(LocalDateTime.now());
            trajetRepo.sauvegarder(trajet);

            envoiRepo.mettreAJourStatutEtTrajet(
                    envoi.getId().toString(), StatutDemandeEnvoi.AFFECTEE,
                    trajet.getId().toString(),
                    trajet.getAffectationId() != null ? trajet.getAffectationId().toString() : null);

            log.info("[Envoi] {} intégré dans trajet {} | {}kg | {} FCFA",
                    envoi.getId(), trajet.getId(), envoi.getPoidsKg(), prixFinal);
            return true;
        }
        return false;
    }

    // ── Confirmer livraison ───────────────────────────────────────────────────

    public DemandeEnvoiResponse confirmerLivraison(String id, String codeConfirmation) {
        DemandeEnvoi envoi = envoiRepo.trouverParId(id)
                .orElseThrow(() -> new DemandeEnvoiNotFoundException(id));

        if (envoi.getStatut() != StatutDemandeEnvoi.EN_COURS)
            throw new IllegalStateException("Impossible de confirmer — statut : " + envoi.getStatut());

        if (!codeConfirmation.equals(envoi.getCodeConfirmation()))
            throw new IllegalArgumentException("Code de confirmation incorrect");

        envoi.setStatut(StatutDemandeEnvoi.LIVREE);
        envoi.setDateConfirmationLivraison(LocalDateTime.now());
        envoiRepo.sauvegarder(envoi);
        log.info("[Envoi] Livraison confirmée : {}", id);
        return versResponse(envoi);
    }

    // ── Annuler ───────────────────────────────────────────────────────────────

    public DemandeEnvoiResponse annuler(String id) {
        DemandeEnvoi envoi = envoiRepo.trouverParId(id)
                .orElseThrow(() -> new DemandeEnvoiNotFoundException(id));

        if (envoi.getStatut() == StatutDemandeEnvoi.EN_COURS
                || envoi.getStatut() == StatutDemandeEnvoi.LIVREE)
            throw new IllegalStateException("Impossible d'annuler — statut : " + envoi.getStatut());

        if (envoi.getTrajetId() != null) {
            trajetRepo.trouverParId(envoi.getTrajetId().toString()).ifPresent(trajet -> {
                trajet.setCapaciteChargementUtiliseeKg(
                        trajet.getCapaciteChargementUtiliseeKg() - envoi.getPoidsKg());
                if (trajet.getColisIds() != null)
                    trajet.getColisIds().remove(envoi.getId().toString());
                trajet.setDateModification(LocalDateTime.now());
                trajetRepo.sauvegarder(trajet);
            });
        }

        envoiRepo.mettreAJourStatut(id, StatutDemandeEnvoi.ANNULEE);
        envoi.setStatut(StatutDemandeEnvoi.ANNULEE);
        return versResponse(envoi);
    }

    // ── Lister ────────────────────────────────────────────────────────────────

    public DemandeEnvoiResponse trouverParId(String id) {
        return versResponse(envoiRepo.trouverParId(id)
                .orElseThrow(() -> new DemandeEnvoiNotFoundException(id)));
    }

    public List<DemandeEnvoiResponse> listerParExpediteur(String expediteurId) {
        return envoiRepo.trouverParExpediteur(expediteurId)
                .stream().map(this::versResponse).collect(Collectors.toList());
    }

    public List<DemandeEnvoiResponse> listerParDestinataire(String destinataireId) {
        return envoiRepo.trouverParDestinataire(destinataireId)
                .stream().map(this::versResponse).collect(Collectors.toList());
    }

    public List<DemandeEnvoiResponse> listerParTrajet(String trajetId) {
        return envoiRepo.trouverParTrajet(trajetId)
                .stream().map(this::versResponse).collect(Collectors.toList());
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private DemandeEnvoiResponse versResponse(DemandeEnvoi d) {
        return DemandeEnvoiResponse.builder()
                .id(d.getId().toString())
                .expediteurId(d.getExpediteurId())
                .destinataireId(d.getDestinataireId())
                .villeDepart(d.getVilleDepart())
                .villeArrivee(d.getVilleArrivee())
                .adresseDepart(d.getAdresseDepart())
                .adresseArrivee(d.getAdresseArrivee())
                .regionDepart(d.getRegionDepart() != null ? d.getRegionDepart().nom : null)
                .regionArrivee(d.getRegionArrivee() != null ? d.getRegionArrivee().nom : null)
                .dateHeureSouhaitee(d.getDateHeureSouhaitee())
                .toleranceMinutes(d.getToleranceMinutes())
                .description(d.getDescription())
                .poidsKg(d.getPoidsKg())
                .categorieColis(d.getCategorieColis())
                .fragile(d.isFragile())
                .dimensionsCm(d.getDimensionsCm())
                .typePaiement(d.getTypePaiement())
                .prixFinal(d.getPrixFinal())
                .statut(d.getStatut())
                .trajetId(d.getTrajetId() != null ? d.getTrajetId().toString() : null)
                .codeConfirmation(d.getCodeConfirmation())
                .dateCreation(d.getDateCreation())
                .dateConfirmationLivraison(d.getDateConfirmationLivraison())
                .build();
    }
}