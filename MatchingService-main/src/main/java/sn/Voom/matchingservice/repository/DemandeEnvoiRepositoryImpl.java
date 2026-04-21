package sn.Voom.matchingservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import sn.Voom.matchingservice.entite.DemandeEnvoi;
import sn.Voom.matchingservice.entite.enums.StatutDemandeEnvoi;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DemandeEnvoiRepositoryImpl implements DemandeEnvoiRepository {

    private final DemandeEnvoiJpaRepository jpa;

    @Override public DemandeEnvoi sauvegarder(DemandeEnvoi d) { return jpa.save(d); }

    @Override public Optional<DemandeEnvoi> trouverParId(String id) {
        return jpa.findById(UUID.fromString(id));
    }

    @Override public List<DemandeEnvoi> trouverParExpediteur(String id) {
        return jpa.findByExpediteurId(id);
    }

    @Override public List<DemandeEnvoi> trouverParDestinataire(String id) {
        return jpa.findByDestinataireId(id);
    }

    @Override public List<DemandeEnvoi> trouverParTrajet(String trajetId) {
        return jpa.findByTrajetId(UUID.fromString(trajetId));
    }

    @Override public List<DemandeEnvoi> trouverParStatut(StatutDemandeEnvoi statut) {
        return jpa.findByStatut(statut);
    }

    @Override public List<DemandeEnvoi> trouverParIds(List<String> ids) {
        List<UUID> uuids = ids.stream().map(UUID::fromString).collect(Collectors.toList());
        return jpa.findAllById(uuids);
    }

    @Override public List<DemandeEnvoi> trouverCandidatesMatching(
            String vD, String vA, LocalDateTime debut, LocalDateTime fin) {
        return jpa.findCandidatesMatching(vD, vA, debut, fin);
    }

    @Override public void mettreAJourStatut(String id, StatutDemandeEnvoi statut) {
        jpa.updateStatut(UUID.fromString(id), statut);
    }

    @Override public void mettreAJourStatutBatch(List<String> ids, StatutDemandeEnvoi statut) {
        if (ids == null || ids.isEmpty()) return;
        List<UUID> uuids = ids.stream().map(UUID::fromString).collect(Collectors.toList());
        jpa.updateStatutBatch(uuids, statut);
    }

    @Override public void mettreAJourStatutEtTrajet(String id, StatutDemandeEnvoi statut,
                                                    String trajetId, String affectationId) {
        jpa.updateStatutEtTrajet(
                UUID.fromString(id), statut,
                UUID.fromString(trajetId),
                UUID.fromString(affectationId));
    }

    @Override public void mettreAJourPrixFinal(String id, float prixFinal) {
        jpa.updatePrixFinal(UUID.fromString(id), prixFinal);
    }

    @Override public void supprimerParId(String id) {
        jpa.deleteById(UUID.fromString(id));
    }
}