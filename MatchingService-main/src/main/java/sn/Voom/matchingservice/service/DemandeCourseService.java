package sn.Voom.matchingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sn.Voom.matchingservice.configuration.MatchingProperties;
import sn.Voom.matchingservice.dto.finance.DetailTarif;
import sn.Voom.matchingservice.dto.request.CreerDemandeCourseRequest;
import sn.Voom.matchingservice.dto.response.DemandeCourseResponse;
import sn.Voom.matchingservice.entite.DemandeCourse;
import sn.Voom.matchingservice.entite.Trajet;
import sn.Voom.matchingservice.entite.enums.StatutDemande;
import sn.Voom.matchingservice.entite.enums.TypeCourse;
import sn.Voom.matchingservice.exception.DemandeCourseNotFoundException;
import sn.Voom.matchingservice.repository.BagageRepository;
import sn.Voom.matchingservice.repository.DemandeCourseRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemandeCourseService {

    private final DemandeCourseRepository demandeRepo;
    private final MatchingService         matchingService;
    private final TarificationService     tarificationService;
    private final MatchingProperties      props;
    private final BagageRepository        bagageRepo;

    private static final int   CAPACITE_PLACES_DEFAUT     = 4;
    private static final float CAPACITE_CHARGEMENT_DEFAUT = 50f;

    // ── Créer une demande ─────────────────────────────────────────────────────

    public DemandeCourseResponse creer(CreerDemandeCourseRequest req) {
        DemandeCourse demande = DemandeCourse.builder()
                .passagerId(req.getPassagerId())
                .typeCourse(req.getTypeCourse())
                .typePaiement(req.getTypePaiement())
                .villeDepart(req.getVilleDepart())
                .villeArrivee(req.getVilleArrivee())
                .regionDepart(req.getRegionDepart())    // ✅ région explicite
                .regionArrivee(req.getRegionArrivee())  // ✅ région explicite
                .coordsDepart(req.getCoordonneesDepart().toPoint())
                .coordsArrivee(req.getCoordonneesArrivee().toPoint())
                .adresseDepart(req.getCoordonneesDepart().getAdresse())
                .adresseArrivee(req.getCoordonneesArrivee().getAdresse())
                .dateHeureDepart(req.getDateHeureDepart())
                .toleranceMinutes(req.getToleranceMinutes())
                .nombrePlaces(req.getNombrePlaces())
                .nombreBagages(req.getNombreBagages())
                .poidsEstimeBagagesKg(req.getPoidsEstimeBagagesKg())
                .descriptionBagages(req.getDescriptionBagages())
                .invitationTrajetId(req.getInvitationTrajetId() != null
                        ? UUID.fromString(req.getInvitationTrajetId())
                        : null)
                .statut(StatutDemande.EN_ATTENTE)
                .conducteursRefusIds(new ArrayList<>())
                .build();

        demandeRepo.sauvegarder(demande);
        log.info("[Demande] Créée : {} par passager {} ({} place(s))",
                demande.getId(), demande.getPassagerId(), demande.getNombrePlaces());

        // ✅ Calcul automatique du prix via la matrice tarifaire sénégalaise
        DetailTarif detail = tarificationService.calculerTarifDepuisDemande(demande);
        demande.setPrixCalcule(detail.getTotalPassager());
        demandeRepo.sauvegarder(demande);
        log.info("[Demande] Prix calculé : {} FCFA ({} → {} | {} place(s))",
                detail.getTotalPassager(), detail.getRegionDepart(),
                detail.getRegionArrivee(), demande.getNombrePlaces());

        // ── Auto-intégration dans un trajet existant ──────────────────────────
        boolean integree = matchingService.tenterAutoIntegration(
                demande, CAPACITE_PLACES_DEFAUT, CAPACITE_CHARGEMENT_DEFAUT);

        if (!integree) {
            demande = demandeRepo.trouverParId(String.valueOf(demande.getId())).orElse(demande);
            declencherBatchSiSeuil(
                    demande.getVilleDepart(),
                    demande.getVilleArrivee(),
                    demande.getTypeCourse());
        }

        return versResponse(demandeRepo.trouverParId(String.valueOf(demande.getId())).orElse(demande));
    }

    // ── Batch matching ────────────────────────────────────────────────────────

    private void declencherBatchSiSeuil(String villeDepart,
                                        String villeArrivee,
                                        TypeCourse typeCourse) {
        LocalDateTime debut = LocalDateTime.now().minusHours(2);
        LocalDateTime fin   = LocalDateTime.now().plusDays((long) props.getFenetreJoursAvance());

        List<DemandeCourse> enAttente = demandeRepo.trouverCandidatesMatching(
                villeDepart, villeArrivee, typeCourse, debut, fin);

        if (enAttente.isEmpty()) return;

        log.info("[Batch] {} demande(s) EN_ATTENTE sur {}→{}",
                enAttente.size(), villeDepart, villeArrivee);

        Map<LocalDateTime, List<DemandeCourse>> parCreneau = grouperParCreneau(enAttente);

        for (Map.Entry<LocalDateTime, List<DemandeCourse>> entry : parCreneau.entrySet()) {
            List<DemandeCourse> groupe = entry.getValue();

            int placesCumulees    = groupe.stream().mapToInt(DemandeCourse::getNombrePlaces).sum();
            boolean vehiculePlein = placesCumulees >= CAPACITE_PLACES_DEFAUT;
            boolean seuilAtteint  = groupe.size() >= props.getSeuilBatch();

            log.info("[Batch] Créneau {} → {} demande(s) | {} place(s) | plein={} seuil={}",
                    entry.getKey(), groupe.size(), placesCumulees, vehiculePlein, seuilAtteint);

            if (vehiculePlein || seuilAtteint) {
                List<String> ids = groupe.stream()
                        .map(d -> d.getId().toString())
                        .collect(Collectors.toList());
                demandeRepo.mettreAJourStatutBatch(ids, StatutDemande.EN_COURS_MATCHING);

                log.info("[Batch] Matching : {} demande(s) | {} place(s) sur {}→{} à {}",
                        groupe.size(), placesCumulees, villeDepart, villeArrivee, entry.getKey());

                List<Trajet> trajets = matchingService.executerMatching(
                        groupe, CAPACITE_PLACES_DEFAUT, CAPACITE_CHARGEMENT_DEFAUT);

                log.info("[Batch] {} trajet(s) créé(s)", trajets.size());
            }
        }
    }

    // ── Groupement par créneau horaire ────────────────────────────────────────

    private Map<LocalDateTime, List<DemandeCourse>> grouperParCreneau(
            List<DemandeCourse> demandes) {
        Map<LocalDateTime, List<DemandeCourse>> groupes = new java.util.LinkedHashMap<>();
        for (DemandeCourse d : demandes) {
            LocalDateTime cle = d.getDateHeureDepart()
                    .truncatedTo(java.time.temporal.ChronoUnit.HOURS);
            groupes.computeIfAbsent(cle, k -> new ArrayList<>()).add(d);
        }
        return groupes;
    }

    // ── Lister / Trouver ──────────────────────────────────────────────────────

    public DemandeCourseResponse trouverParId(String id) {
        return versResponse(demandeRepo.trouverParId(id)
                .orElseThrow(() -> new DemandeCourseNotFoundException(id)));
    }

    public List<DemandeCourseResponse> listerParPassager(String passagerId) {
        return demandeRepo.trouverParPassager(passagerId).stream()
                .map(this::versResponse).collect(Collectors.toList());
    }

    public List<DemandeCourseResponse> listerEnAttente() {
        return demandeRepo.trouverParStatut(StatutDemande.EN_ATTENTE).stream()
                .map(this::versResponse).collect(Collectors.toList());
    }

    public List<DemandeCourseResponse> listerParTrajet(String trajetId) {
        return demandeRepo.trouverParIds(
                        demandeRepo.trouverParStatut(StatutDemande.AFFECTEE).stream()
                                .filter(d -> trajetId.equals(
                                        d.getTrajetId() != null
                                                ? d.getTrajetId().toString() : null))
                                .map(d -> d.getId().toString())
                                .collect(Collectors.toList()))
                .stream().map(this::versResponse).collect(Collectors.toList());
    }

    // ── Annuler ───────────────────────────────────────────────────────────────

    public DemandeCourseResponse annuler(String id) {
        DemandeCourse d = demandeRepo.trouverParId(id)
                .orElseThrow(() -> new DemandeCourseNotFoundException(id));

        if (d.getStatut() == StatutDemande.EN_COURS
                || d.getStatut() == StatutDemande.TERMINEE) {
            throw new IllegalStateException(
                    "Impossible d'annuler une demande en cours ou terminée");
        }

        demandeRepo.mettreAJourStatut(id, StatutDemande.ANNULEE);
        d.setStatut(StatutDemande.ANNULEE);
        return versResponse(d);
    }

    // ── Mapper vers Response ──────────────────────────────────────────────────

    public DemandeCourseResponse versResponse(DemandeCourse d) {
        return DemandeCourseResponse.builder()
                .id(String.valueOf(d.getId()))
                .passagerId(d.getPassagerId())
                .typeCourse(d.getTypeCourse())
                .typePaiement(d.getTypePaiement())
                .villeDepart(d.getVilleDepart())
                .villeArrivee(d.getVilleArrivee())
                .regionDepart(d.getRegionDepart() != null ? d.getRegionDepart().nom : null)
                .regionArrivee(d.getRegionArrivee() != null ? d.getRegionArrivee().nom : null)
                .coordonneesDepart(d.getCoordonneesDepart())
                .coordonneesArrivee(d.getCoordonneesArrivee())
                .dateHeureDepart(d.getDateHeureDepart())
                .toleranceMinutes(d.getToleranceMinutes())
                .nombrePlaces(d.getNombrePlaces())
                .nombreBagages(d.getNombreBagages())
                .poidsEstimeBagagesKg(d.getPoidsEstimeBagagesKg())
                .bagagesIds(bagageRepo.trouverParDemande(d.getId().toString())
                        .stream()
                        .map(b -> b.getId().toString())
                        .collect(Collectors.toList()))
                .prixCalcule(d.getPrixCalcule())
                .statut(d.getStatut())
                .affectationId(d.getAffectationId() != null
                        ? d.getAffectationId().toString() : null)
                .trajetId(d.getTrajetId() != null
                        ? d.getTrajetId().toString() : null)
                .dateCreation(d.getDateCreation())
                .build();
    }
}