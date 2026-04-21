package sn.Voom.matchingservice.repository;

import sn.Voom.matchingservice.entite.InvitationTrajet;
import sn.Voom.matchingservice.entite.enums.StatutInvitation;

import java.util.List;
import java.util.Optional;

public interface InvitationTrajetRepository {
    InvitationTrajet sauvegarder(InvitationTrajet invitation);
    Optional<InvitationTrajet> trouverParId(String id);
    List<InvitationTrajet> trouverParInvite(String inviteId);
    List<InvitationTrajet> trouverParTrajet(String trajetId);
    void mettreAJourStatut(String id, StatutInvitation statut);
}
