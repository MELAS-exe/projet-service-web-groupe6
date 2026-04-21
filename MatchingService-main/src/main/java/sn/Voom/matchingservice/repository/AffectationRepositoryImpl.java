package sn.Voom.matchingservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import sn.Voom.matchingservice.entite.Affectation;
import sn.Voom.matchingservice.entite.enums.StatutAffectation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AffectationRepositoryImpl implements AffectationRepository {

    private final AffectationJpaRepository jpa;

    @Override public Affectation sauvegarder(Affectation a) { return jpa.save(a); }

    @Override public Optional<Affectation> trouverParId(String id) {
        return jpa.findById(UUID.fromString(id));
    }

    @Override public List<Affectation> trouverParConducteur(String id) {
        return jpa.findByConducteurId(id);
    }

    @Override public List<Affectation> trouverParStatut(StatutAffectation s) {
        return jpa.findByStatut(s);
    }

    @Override public List<Affectation> trouverParTrajet(String id) {
        return jpa.findByTrajetId(UUID.fromString(id));
    }

    @Override public List<Affectation> trouverExpirees() {
        return jpa.findExpirees(LocalDateTime.now());
    }

    @Override public void mettreAJourStatut(String id, StatutAffectation s) {
        jpa.updateStatut(UUID.fromString(id), s);
    }

    @Override public void ajouterConducteurRefus(String id, String cId) {
        jpa.appendConducteurRefus(id, cId);
    }
}