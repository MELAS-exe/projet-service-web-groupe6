package sn.Voom.matchingservice.repository;

import sn.Voom.matchingservice.entite.Affectation;
import sn.Voom.matchingservice.entite.enums.StatutAffectation;

import java.util.List;
import java.util.Optional;

public interface AffectationRepository {
    Affectation sauvegarder(Affectation affectation);
    Optional<Affectation> trouverParId(String id);
    List<Affectation> trouverParConducteur(String conducteurId);
    List<Affectation> trouverParStatut(StatutAffectation statut);
    List<Affectation> trouverParTrajet(String trajetId);
    /** Affectations PROPOSEE dont la dateExpiration est dépassée. */
    List<Affectation> trouverExpirees();
    void mettreAJourStatut(String id, StatutAffectation statut);
    void ajouterConducteurRefus(String affectationId, String conducteurId);
}
