package sn.Voom.matchingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import sn.Voom.matchingservice.entite.DemandeCourse;
import sn.Voom.matchingservice.entite.enums.StatutDemande;
import sn.Voom.matchingservice.entite.enums.TypeCourse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DemandeCourseJpaRepository extends JpaRepository<DemandeCourse, UUID> {

    List<DemandeCourse> findByStatut(StatutDemande statut);
    List<DemandeCourse> findByPassagerId(String passagerId);

    @Query("""
        SELECT d FROM DemandeCourse d
        WHERE d.villeDepart  = :villeDepart
          AND d.villeArrivee = :villeArrivee
          AND d.typeCourse   = :typeCourse
          AND d.statut       = sn.Voom.matchingservice.entite.enums.StatutDemande.EN_ATTENTE
          AND d.dateHeureDepart BETWEEN :debut AND :fin
        ORDER BY d.dateHeureDepart
    """)
    List<DemandeCourse> findCandidatesMatching(
            @Param("villeDepart")  String villeDepart,
            @Param("villeArrivee") String villeArrivee,
            @Param("typeCourse")   TypeCourse typeCourse,
            @Param("debut")        LocalDateTime debut,
            @Param("fin")          LocalDateTime fin
    );

    @Modifying @Transactional
    @Query("UPDATE DemandeCourse d SET d.statut = :statut WHERE d.id = :id")
    void updateStatut(@Param("id") UUID id, @Param("statut") StatutDemande statut);

    // ── Mise à jour batch (1 requête au lieu de N) ────────────────────────────
    @Modifying @Transactional
    @Query("UPDATE DemandeCourse d SET d.statut = :statut WHERE d.id IN :ids")
    void updateStatutBatch(@Param("ids") List<UUID> ids, @Param("statut") StatutDemande statut);

    @Modifying @Transactional
    @Query("""
        UPDATE DemandeCourse d
        SET d.statut = :statut, d.affectationId = :affectationId, d.trajetId = :trajetId
        WHERE d.id = :id
    """)
    void updateStatutEtAffectation(@Param("id") UUID id,
                                   @Param("statut") StatutDemande statut,
                                   @Param("affectationId") UUID affectationId,
                                   @Param("trajetId") UUID trajetId);

    @Modifying @Transactional
    @Query(value = "UPDATE demandes_course SET conducteurs_refus_ids = array_append(conducteurs_refus_ids, :conducteurId) WHERE id = :id::uuid", nativeQuery = true)
    void appendConducteurRefus(@Param("id") String id, @Param("conducteurId") String conducteurId);
}