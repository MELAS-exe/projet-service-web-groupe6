package sn.Voom.matchingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sn.Voom.matchingservice.dto.response.AffectationResponse;
import sn.Voom.matchingservice.entite.Affectation;
import sn.Voom.matchingservice.entite.Trajet;
import sn.Voom.matchingservice.entite.enums.StatutAffectation;
import sn.Voom.matchingservice.entite.enums.StatutDemande;
import sn.Voom.matchingservice.entite.enums.StatutTrajet;
import sn.Voom.matchingservice.exception.AffectationNotFoundException;
import sn.Voom.matchingservice.repository.AffectationRepository;
import sn.Voom.matchingservice.repository.DemandeCourseRepository;
import sn.Voom.matchingservice.repository.TrajetRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestion des Affectations conducteur ↔ groupe.
 *
 * Cas couverts :
 *   - Conducteur accepte   → trajet passe à PROGRAMME
 *   - Conducteur refuse    → récupération automatique du groupe
 *   - Expiration scheduler → récupération automatique toutes les 5 min
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AffectationService {

    private final AffectationRepository affectationRepo;
    private final TrajetRepository trajetRepo;
    private final DemandeCourseRepository demandeRepo;
    private final MatchingService matchingService;

    private static final int CAPACITE_PLACES_DEFAUT = 4;
    private static final float CAPACITE_CHARGEMENT_DEFAUT = 50f;

    // ── Conductor accepts ─────────────────────────────────────────────────────

    /**
     * Le conducteur accepte l'affectation.
     * → Affectation passe en ACCEPTEE
     * → Trajet passe en PROGRAMME avec conducteurId + vehiculeId
     * → Demandes passent en CONFIRMEE
     */
    public AffectationResponse accepter(String affectationId, String conducteurId, String vehiculeId) {
        Affectation aff = trouverOuLever(affectationId);

        if (aff.getStatut() != StatutAffectation.PROPOSEE) {
            throw new IllegalStateException("L'affectation n'est plus en statut PROPOSEE : " + aff.getStatut());
        }

        // Mettre à jour l'affectation
        aff.setConducteurId(conducteurId);
        aff.setStatut(StatutAffectation.ACCEPTEE);
        affectationRepo.sauvegarder(aff);

        // Mettre à jour le trajet
        trajetRepo.assignerConducteur(String.valueOf(aff.getTrajetId()), conducteurId, vehiculeId);

        // Confirmer toutes les demandes
        aff.getDemandesIds().forEach(id ->
                demandeRepo.mettreAJourStatut(id, StatutDemande.CONFIRMEE));

        log.info("[Affectation] Acceptée : {} par conducteur {}", affectationId, conducteurId);
        return versResponse(aff);
    }

    // ── Conductor refuses ─────────────────────────────────────────────────────

    /**
     * Le conducteur refuse l'affectation.
     * → Déclenche la récupération du groupe
     */
    public AffectationResponse refuser(String affectationId, String conducteurId) {
        Affectation aff = trouverOuLever(affectationId);

        if (aff.getStatut() != StatutAffectation.PROPOSEE) {
            throw new IllegalStateException("L'affectation ne peut plus être refusée : " + aff.getStatut());
        }

        log.info("[Affectation] Refusée : {} par conducteur {}", affectationId, conducteurId);

        matchingService.recupererApreRefusOuExpiration(
                affectationId, conducteurId,
                StatutAffectation.REFUSEE,
                CAPACITE_PLACES_DEFAUT, CAPACITE_CHARGEMENT_DEFAUT);

        return versResponse(affectationRepo.trouverParId(affectationId).orElse(aff));
    }

    // ── Scheduled: expire stale affectations every 5 minutes ─────────────────

    /**
     * Vérifie toutes les 5 minutes les affectations PROPOSEE expirées
     * et déclenche la récupération automatique.
     */
    /**
     * Vérifie toutes les 5 minutes les affectations PROPOSEE expirées.
     *
     * ── Résilience aux index Firestore en construction ───────────────────────
     * Au premier démarrage, l'index composite (statut + dateExpiration) peut
     * ne pas être encore prêt (FAILED_PRECONDITION / "index currently building").
     * On intercepte cette erreur pour ne pas crasher le scheduler ; il réessaiera
     * automatiquement au prochain tick.
     * Solution permanente : déployer firestore.indexes.json via Firebase CLI.
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000) // 5 min
    public void traiterAffectationsExpirees() {
        List<Affectation> expirees;
        try {
            expirees = affectationRepo.trouverExpirees();
        } catch (sn.Voom.matchingservice.exception.FirestoreOperationException e) {
            Throwable cause = e.getCause();
            if (cause != null && cause.getMessage() != null
                    && cause.getMessage().contains("FAILED_PRECONDITION")) {
                // Index composite encore en construction – on attend le prochain tick
                log.warn("[Scheduler] Index Firestore 'affectations' pas encore prêt – " +
                        "réessai dans 5 min. Déployez firestore.indexes.json pour accélérer.");
                return;
            }
            log.error("[Scheduler] Erreur Firestore inattendue", e);
            return;
        } catch (Exception e) {
            log.error("[Scheduler] Erreur inattendue", e);
            return;
        }

        if (expirees.isEmpty()) return;

        log.info("[Scheduler] {} affectation(s) expirée(s) à récupérer", expirees.size());
        for (Affectation aff : expirees) {
            try {
                matchingService.recupererApreRefusOuExpiration(
                        String.valueOf(aff.getId()), null,
                        StatutAffectation.EXPIREE,
                        CAPACITE_PLACES_DEFAUT, CAPACITE_CHARGEMENT_DEFAUT);
            } catch (Exception e) {
                log.error("[Scheduler] Erreur récupération affectation {}", aff.getId(), e);
            }
        }
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public AffectationResponse trouverParId(String id) {
        return versResponse(trouverOuLever(id));
    }

    public List<AffectationResponse> listerParConducteur(String conducteurId) {
        return affectationRepo.trouverParConducteur(conducteurId).stream()
                .map(this::versResponse).collect(Collectors.toList());
    }

    public List<AffectationResponse> listerParStatut(String statut) {
        StatutAffectation s = StatutAffectation.valueOf(statut.toUpperCase());
        return affectationRepo.trouverParStatut(s).stream()
                .map(this::versResponse).collect(Collectors.toList());
    }

    // ── Utils ─────────────────────────────────────────────────────────────────

    private Affectation trouverOuLever(String id) {
        return affectationRepo.trouverParId(id)
                .orElseThrow(() -> new AffectationNotFoundException(id));
    }

    public AffectationResponse versResponse(Affectation a) {
        return AffectationResponse.builder()
                .id(String.valueOf(a.getId()))
                .trajetId(String.valueOf(a.getTrajetId()))
                .conducteurId(a.getConducteurId())
                .demandesIds(a.getDemandesIds())
                .revenusEstimes(a.getRevenusEstimes())
                .distanceTotaleKm(a.getDistanceTotaleKm())
                .dateAffectation(a.getDateAffectation())
                .dateExpiration(a.getDateExpiration())
                .statut(a.getStatut())
                .tentativeNumero(a.getTentativeNumero())
                .conducteursRefusIds(a.getConducteursRefusIds())
                .build();
    }
}