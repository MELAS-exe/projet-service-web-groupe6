package sn.Voom.matchingservice.repository;

import sn.Voom.matchingservice.entite.DemandeCourse;
import sn.Voom.matchingservice.entite.enums.StatutDemande;
import sn.Voom.matchingservice.entite.enums.TypeCourse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DemandeCourseRepository {

    DemandeCourse sauvegarder(DemandeCourse d);
    Optional<DemandeCourse> trouverParId(String id);
    List<DemandeCourse> trouverParStatut(StatutDemande statut);
    List<DemandeCourse> trouverParPassager(String passagerId);
    List<DemandeCourse> trouverParIds(List<String> ids);
    void supprimerParId(String id);

    List<DemandeCourse> trouverCandidatesMatching(
            String villeDepart, String villeArrivee,
            TypeCourse typeCourse,
            LocalDateTime debut, LocalDateTime fin);

    void mettreAJourStatut(String id, StatutDemande statut);
    void mettreAJourStatutBatch(List<String> ids, StatutDemande statut); // ← nouveau
    void mettreAJourStatutEtAffectation(String id, StatutDemande statut,
                                        String affectationId, String trajetId);
    void mettreAJourStatutEtAffectation(UUID id, StatutDemande statut,
                                        UUID affectationId, UUID trajetId);
    void ajouterConducteurRefus(String id, String conducteurId);
}