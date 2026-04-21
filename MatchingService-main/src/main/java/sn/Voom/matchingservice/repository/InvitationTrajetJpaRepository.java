package sn.Voom.matchingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import sn.Voom.matchingservice.entite.InvitationTrajet;
import sn.Voom.matchingservice.entite.enums.StatutInvitation;

import java.util.List;
import java.util.UUID;

public interface InvitationTrajetJpaRepository extends JpaRepository<InvitationTrajet, UUID> {

    List<InvitationTrajet> findByInviteId(String inviteId);
    List<InvitationTrajet> findByTrajetId(UUID trajetId);  // UUID car trajetId est UUID en base

    @Modifying @Transactional
    @Query("UPDATE InvitationTrajet i SET i.statut = :statut, i.dateReponse = CURRENT_TIMESTAMP WHERE i.id = :id")
    void updateStatut(@Param("id") UUID id, @Param("statut") StatutInvitation statut);
}