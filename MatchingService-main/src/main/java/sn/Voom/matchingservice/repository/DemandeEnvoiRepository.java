package sn.Voom.matchingservice.repository;

import sn.Voom.matchingservice.entite.DemandeEnvoi;
import sn.Voom.matchingservice.entite.enums.StatutDemandeEnvoi;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DemandeEnvoiRepository {

    DemandeEnvoi sauvegarder(DemandeEnvoi d);
    Optional<DemandeEnvoi> trouverParId(String id);
    List<DemandeEnvoi> trouverParExpediteur(String expediteurId);
    List<DemandeEnvoi> trouverParDestinataire(String destinataireId);
    List<DemandeEnvoi> trouverParTrajet(String trajetId);
    List<DemandeEnvoi> trouverParStatut(StatutDemandeEnvoi statut);
    List<DemandeEnvoi> trouverParIds(List<String> ids);

    List<DemandeEnvoi> trouverCandidatesMatching(
            String villeDepart, String villeArrivee,
            LocalDateTime debut, LocalDateTime fin);

    void mettreAJourStatut(String id, StatutDemandeEnvoi statut);
    void mettreAJourStatutBatch(List<String> ids, StatutDemandeEnvoi statut);
    void mettreAJourStatutEtTrajet(String id, StatutDemandeEnvoi statut,
                                   String trajetId, String affectationId);
    void mettreAJourPrixFinal(String id, float prixFinal);
    void supprimerParId(String id);
}