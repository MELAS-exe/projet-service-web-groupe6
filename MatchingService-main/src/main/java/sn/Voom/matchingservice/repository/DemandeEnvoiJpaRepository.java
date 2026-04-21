package sn.Voom.matchingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import sn.Voom.matchingservice.entite.DemandeEnvoi;
import sn.Voom.matchingservice.entite.enums.StatutDemandeEnvoi;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DemandeEnvoiJpaRepository extends JpaRepository<DemandeEnvoi, UUID> {

    List<DemandeEnvoi> findByExpediteurId(String expediteurId);
    List<DemandeEnvoi> findByDestinataireId(String destinataireId);
    List<DemandeEnvoi> findByTrajetId(UUID trajetId);
    List<DemandeEnvoi> findByStatut(StatutDemandeEnvoi statut);

    // Candidates matching — uniquement EN_ATTENTE sur la même route
    @Query("""
        SELECT d FROM DemandeEnvoi d
        WHERE d.villeDepart  = :villeDepart
          AND d.villeArrivee = :villeArrivee
          AND d.statut       = sn.Voom.matchingservice.entite.enums.StatutDemandeEnvoi.EN_ATTENTE
          AND d.dateHeureSouhaitee BETWEEN :debut AND :fin
        ORDER BY d.dateHeureSouhaitee
    """)
    List<DemandeEnvoi> findCandidatesMatching(
            @Param("villeDepart")  String villeDepart,
            @Param("villeArrivee") String villeArrivee,
            @Param("debut")        LocalDateTime debut,
            @Param("fin")          LocalDateTime fin
    );

    @Modifying @Transactional
    @Query("UPDATE DemandeEnvoi d SET d.statut = :statut WHERE d.id = :id")
    void updateStatut(@Param("id") UUID id, @Param("statut") StatutDemandeEnvoi statut);

    @Modifying @Transactional
    @Query("UPDATE DemandeEnvoi d SET d.statut = :statut WHERE d.id IN :ids")
    void updateStatutBatch(@Param("ids") List<UUID> ids,
                           @Param("statut") StatutDemandeEnvoi statut);

    @Modifying @Transactional
    @Query("""
        UPDATE DemandeEnvoi d
        SET d.statut = :statut, d.trajetId = :trajetId, d.affectationId = :affectationId
        WHERE d.id = :id
    """)
    void updateStatutEtTrajet(@Param("id")            UUID id,
                              @Param("statut")        StatutDemandeEnvoi statut,
                              @Param("trajetId")      UUID trajetId,
                              @Param("affectationId") UUID affectationId);

    @Modifying @Transactional
    @Query("UPDATE DemandeEnvoi d SET d.prixFinal = :prixFinal WHERE d.id = :id")
    void updatePrixFinal(@Param("id") UUID id, @Param("prixFinal") float prixFinal);
}