package sn.Voom.matchingservice.repository;

import sn.Voom.matchingservice.entite.Bagage;
import sn.Voom.matchingservice.entite.enums.StatutBagage;

import java.util.List;
import java.util.Optional;

public interface BagageRepository {
    Bagage sauvegarder(Bagage b);
    Optional<Bagage> trouverParId(String id);
    List<Bagage> trouverParDemande(String demandeId);
    List<Bagage> trouverParDemande(List<String> demandesIds); // ← pour le résumé financier
    List<Bagage> trouverParExpediteur(String expediteurId);
    void mettreAJourStatut(String id, StatutBagage statut);
    void supprimerParId(String id);
}