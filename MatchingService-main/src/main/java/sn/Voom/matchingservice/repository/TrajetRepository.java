package sn.Voom.matchingservice.repository;

import sn.Voom.matchingservice.entite.Trajet;
import sn.Voom.matchingservice.entite.enums.StatutTrajet;

import java.util.List;
import java.util.Optional;

public interface TrajetRepository {
    Trajet sauvegarder(Trajet trajet);
    Optional<Trajet> trouverParId(String id);
    List<Trajet> trouverParConducteur(String conducteurId);
    List<Trajet> trouverParStatut(StatutTrajet statut);
    void mettreAJourStatut(String id, StatutTrajet statut);
    void assignerConducteur(String trajetId, String conducteurId, String vehiculeId);
    void supprimerParId(String id);
}
