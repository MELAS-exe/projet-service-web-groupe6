package sn.Voom.matchingservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import sn.Voom.matchingservice.entite.Trajet;
import sn.Voom.matchingservice.entite.enums.StatutTrajet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TrajetRepositoryImpl implements TrajetRepository {

    private final TrajetJpaRepository jpa;

    @Override public Trajet sauvegarder(Trajet t) { return jpa.save(t); }

    @Override public Optional<Trajet> trouverParId(String id) {
        return jpa.findById(UUID.fromString(id));
    }

    @Override public List<Trajet> trouverParConducteur(String id) {
        return jpa.findByConducteurId(id);
    }

    @Override public List<Trajet> trouverParStatut(StatutTrajet s) {
        return jpa.findByStatut(s);
    }

    @Override public void mettreAJourStatut(String id, StatutTrajet s) {
        jpa.updateStatut(UUID.fromString(id), s);
    }

    @Override public void assignerConducteur(String tId, String cId, String vId) {
        jpa.assignerConducteur(UUID.fromString(tId), cId, vId);
    }

    @Override public void supprimerParId(String id) {
        jpa.deleteById(UUID.fromString(id));
    }
}