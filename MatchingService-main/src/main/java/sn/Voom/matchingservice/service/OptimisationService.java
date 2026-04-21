package sn.Voom.matchingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sn.Voom.matchingservice.configuration.MatchingProperties;
import sn.Voom.matchingservice.entite.DemandeCourse;
import sn.Voom.matchingservice.entite.enums.StatutDemande;
import sn.Voom.matchingservice.repository.DemandeCourseRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ════════════════════════════════════════════════════════════════════════════
 * OPTIMISATION CONTINUE DES GROUPES
 * ════════════════════════════════════════════════════════════════════════════
 *
 * Stratégie :
 *   - Scheduler adaptatif : tourne plus vite en pic, plus lentement en creux
 *   - Inactif la nuit (22h → 6h) pour économiser les ressources Cloud
 *   - 1 seule requête SQL pour tout récupérer, groupement en mémoire
 *   - 1 requête batch pour verrouiller les demandes (évite les doublons)
 *
 * Déclenchement du matching si :
 *   - Groupe plein (capacité véhicule atteinte) → immédiat
 *   - Demande la plus ancienne > delaiAttenteOptimisationMinutes → forcé
 *   - Sinon → attendre de meilleures correspondances
 * ════════════════════════════════════════════════════════════════════════════
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OptimisationService {

    private static final int CAPACITE_PLACES_DEFAUT     = 4;
    private static final float CAPACITE_CHARGEMENT_DEFAUT = 50f;

    private final DemandeCourseRepository demandeRepo;
    private final MatchingService matchingService;
    private final MatchingProperties props;

    // Compteur interne pour adapter l'intervalle
    private volatile int demandesEnAttenteCount = 0;

    /**
     * Scheduler principal — intervalle creux (5 min par défaut).
     * S'adapte automatiquement selon le volume de demandes.
     */
    @Scheduled(fixedDelayString = "${matching.intervalle-scheduler-creux-ms:300000}")
    public void optimiserGroupesEnAttente() {

        // ── 1. Plage horaire active uniquement ────────────────────────────
        int heure = LocalDateTime.now().getHour();
        if (heure < props.getSchedulerActifHeuresDebut()
                || heure >= props.getSchedulerActifHeuresFin()) {
            log.debug("[Optimisation] Hors plage horaire ({}h), skip", heure);
            return;
        }

        // ── 2. UNE seule requête SQL pour tout récupérer ──────────────────
        List<DemandeCourse> toutesEnAttente = demandeRepo
                .trouverParStatut(StatutDemande.EN_ATTENTE);

        demandesEnAttenteCount = toutesEnAttente.size();

        if (toutesEnAttente.isEmpty()) {
            log.debug("[Optimisation] Aucune demande en attente");
            return;
        }

        log.info("[Optimisation] {} demande(s) EN_ATTENTE à évaluer", demandesEnAttenteCount);

        // ── 3. Groupement en mémoire par ville+type ───────────────────────
        //    (pas de requêtes SQL supplémentaires)
        Map<String, List<DemandeCourse>> parRoute = toutesEnAttente.stream()
                .collect(Collectors.groupingBy(d ->
                        d.getVilleDepart() + "_" +
                                d.getVilleArrivee() + "_" +
                                d.getTypeCourse()));

        parRoute.forEach((route, demandes) -> {
            // Re-grouper par créneau horaire
            grouperParCreneau(demandes).forEach((creneau, groupe) ->
                    evaluerEtMatcher(groupe));
        });
    }

    /**
     * Scheduler pic — tourne plus fréquemment quand il y a beaucoup de demandes.
     */
    @Scheduled(fixedDelayString = "${matching.intervalle-scheduler-pic-ms:60000}")
    public void optimiserEnPic() {
        // N'intervient que si on est en période de pic
        if (demandesEnAttenteCount < props.getSeuilPicDemandes()) {
            return;
        }
        log.debug("[Optimisation-Pic] {} demandes en attente → mode pic actif",
                demandesEnAttenteCount);
        optimiserGroupesEnAttente();
    }

    // ── Logique d'évaluation ──────────────────────────────────────────────────

    private void evaluerEtMatcher(List<DemandeCourse> groupe) {
        if (groupe.isEmpty()) return;

        DemandeCourse ref       = groupe.get(0);
        boolean groupePlein     = groupe.size() >= CAPACITE_PLACES_DEFAUT;
        boolean demandeVieille  = estTropVieux(groupe);

        if (groupePlein || demandeVieille) {
            log.info("[Optimisation] Matching déclenché : {} demandes {}→{} | plein={} vieux={}",
                    groupe.size(), ref.getVilleDepart(), ref.getVilleArrivee(),
                    groupePlein, demandeVieille);

            // ── Verrouiller en UNE seule requête batch ────────────────────
            List<String> ids = groupe.stream()
                    .map(d -> d.getId().toString())
                    .collect(Collectors.toList());
            demandeRepo.mettreAJourStatutBatch(ids, StatutDemande.EN_COURS_MATCHING);

            // ── Lancer le matching ────────────────────────────────────────
            matchingService.executerMatching(
                    groupe, CAPACITE_PLACES_DEFAUT, CAPACITE_CHARGEMENT_DEFAUT);

        } else {
            log.debug("[Optimisation] Attente : {} demande(s) {}→{} | âge={}min",
                    groupe.size(), ref.getVilleDepart(), ref.getVilleArrivee(),
                    calculerAgeMaxMinutes(groupe));
        }
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    private Map<LocalDateTime, List<DemandeCourse>> grouperParCreneau(
            List<DemandeCourse> demandes) {
        return demandes.stream()
                .collect(Collectors.groupingBy(d ->
                        d.getDateHeureDepart()
                                .truncatedTo(java.time.temporal.ChronoUnit.HOURS)));
    }

    private boolean estTropVieux(List<DemandeCourse> groupe) {
        long ageMax = calculerAgeMaxMinutes(groupe);
        return ageMax >= props.getDelaiAttenteOptimisationMinutes();
    }

    private long calculerAgeMaxMinutes(List<DemandeCourse> groupe) {
        return groupe.stream()
                .map(DemandeCourse::getDateCreation)
                .min(LocalDateTime::compareTo)
                .map(plusAncienne -> ChronoUnit.MINUTES.between(
                        plusAncienne, LocalDateTime.now()))
                .orElse(0L);
    }

    public int getDemandesEnAttenteCount() {
        return demandesEnAttenteCount;
    }
}