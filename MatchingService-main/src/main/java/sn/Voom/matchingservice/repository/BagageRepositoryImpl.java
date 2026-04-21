package sn.Voom.matchingservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import sn.Voom.matchingservice.entite.Bagage;
import sn.Voom.matchingservice.entite.enums.StatutBagage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BagageRepositoryImpl implements BagageRepository {

    private final BagageJpaRepository jpa;

    @Override public Bagage sauvegarder(Bagage b) { return jpa.save(b); }

    @Override public Optional<Bagage> trouverParId(String id) {
        return jpa.findById(UUID.fromString(id));
    }

    @Override public List<Bagage> trouverParDemande(String demandeId) {
        return jpa.findByDemandeId(UUID.fromString(demandeId));
    }

    // ← Pour le résumé financier — tous les bagages de plusieurs demandes
    @Override public List<Bagage> trouverParDemande(List<String> demandesIds) {
        List<UUID> uuids = demandesIds.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());
        return jpa.findByDemandeIdIn(uuids);
    }

    @Override public List<Bagage> trouverParExpediteur(String expediteurId) {
        return jpa.findByExpediteurId(expediteurId);
    }

    @Override public void mettreAJourStatut(String id, StatutBagage statut) {
        jpa.updateStatut(UUID.fromString(id), statut);
    }

    @Override public void supprimerParId(String id) {
        jpa.deleteById(UUID.fromString(id));
    }
}