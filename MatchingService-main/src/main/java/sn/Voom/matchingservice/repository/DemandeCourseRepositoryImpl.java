package sn.Voom.matchingservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import sn.Voom.matchingservice.entite.DemandeCourse;
import sn.Voom.matchingservice.entite.enums.StatutDemande;
import sn.Voom.matchingservice.entite.enums.TypeCourse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DemandeCourseRepositoryImpl implements DemandeCourseRepository {

    private final DemandeCourseJpaRepository jpa;

    @Override public DemandeCourse sauvegarder(DemandeCourse d) { return jpa.save(d); }

    @Override public Optional<DemandeCourse> trouverParId(String id) {
        return jpa.findById(UUID.fromString(id));
    }

    @Override public List<DemandeCourse> trouverParStatut(StatutDemande s) {
        return jpa.findByStatut(s);
    }

    @Override public List<DemandeCourse> trouverParPassager(String id) {
        return jpa.findByPassagerId(id);
    }

    @Override public List<DemandeCourse> trouverParIds(List<String> ids) {
        List<UUID> uuids = ids.stream().map(UUID::fromString).collect(Collectors.toList());
        return jpa.findAllById(uuids);
    }

    @Override public void supprimerParId(String id) {
        jpa.deleteById(UUID.fromString(id));
    }

    @Override public List<DemandeCourse> trouverCandidatesMatching(
            String vD, String vA, TypeCourse tc, LocalDateTime debut, LocalDateTime fin) {
        return jpa.findCandidatesMatching(vD, vA, tc, debut, fin);
    }

    @Override public void mettreAJourStatut(String id, StatutDemande s) {
        jpa.updateStatut(UUID.fromString(id), s);
    }

    // ── Batch : 1 requête SQL au lieu de N ────────────────────────────────────
    @Override public void mettreAJourStatutBatch(List<String> ids, StatutDemande statut) {
        if (ids == null || ids.isEmpty()) return;
        List<UUID> uuids = ids.stream().map(UUID::fromString).collect(Collectors.toList());
        jpa.updateStatutBatch(uuids, statut);
    }

    @Override public void mettreAJourStatutEtAffectation(
            UUID id, StatutDemande statut, UUID affectationId, UUID trajetId) {
        jpa.updateStatutEtAffectation(id, statut, affectationId, trajetId);
    }

    @Override public void mettreAJourStatutEtAffectation(
            String id, StatutDemande s, String aId, String tId) {
        jpa.updateStatutEtAffectation(
                UUID.fromString(id), s,
                aId != null ? UUID.fromString(aId) : null,
                tId != null ? UUID.fromString(tId) : null);
    }

    @Override public void ajouterConducteurRefus(String id, String cId) {
        jpa.appendConducteurRefus(id, cId);
    }
}