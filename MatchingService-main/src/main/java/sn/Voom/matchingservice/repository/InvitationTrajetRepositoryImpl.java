package sn.Voom.matchingservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import sn.Voom.matchingservice.entite.InvitationTrajet;
import sn.Voom.matchingservice.entite.enums.StatutInvitation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class InvitationTrajetRepositoryImpl implements InvitationTrajetRepository {

    private final InvitationTrajetJpaRepository jpa;

    @Override public InvitationTrajet sauvegarder(InvitationTrajet i) { return jpa.save(i); }

    @Override public Optional<InvitationTrajet> trouverParId(String id) {
        return jpa.findById(UUID.fromString(id));
    }

    @Override public List<InvitationTrajet> trouverParInvite(String id) {
        return jpa.findByInviteId(id);
    }

    @Override public List<InvitationTrajet> trouverParTrajet(String id) {
        return jpa.findByTrajetId(UUID.fromString(id));
    }

    @Override public void mettreAJourStatut(String id, StatutInvitation s) {
        jpa.updateStatut(UUID.fromString(id), s);
    }
}