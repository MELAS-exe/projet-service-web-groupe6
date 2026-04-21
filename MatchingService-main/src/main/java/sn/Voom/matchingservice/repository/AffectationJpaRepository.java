package sn.Voom.matchingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import sn.Voom.matchingservice.entite.Affectation;
import sn.Voom.matchingservice.entite.enums.StatutAffectation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AffectationJpaRepository extends JpaRepository<Affectation, UUID> {

    List<Affectation> findByConducteurId(String conducteurId);
    List<Affectation> findByStatut(StatutAffectation statut);
    List<Affectation> findByTrajetId(UUID trajetId);  // UUID car trajetId est UUID en base

    @Query("SELECT a FROM Affectation a WHERE a.statut = sn.Voom.matchingservice.entite.enums.StatutAffectation.PROPOSEE AND a.dateExpiration < :maintenant")
    List<Affectation> findExpirees(@Param("maintenant") LocalDateTime maintenant);

    @Modifying @Transactional
    @Query("UPDATE Affectation a SET a.statut = :statut WHERE a.id = :id")
    void updateStatut(@Param("id") UUID id, @Param("statut") StatutAffectation statut);

    @Modifying @Transactional
    @Query(value = "UPDATE affectations SET conducteurs_refus_ids = array_append(conducteurs_refus_ids, :conducteurId) WHERE id = :id::uuid", nativeQuery = true)
    void appendConducteurRefus(@Param("id") String id, @Param("conducteurId") String conducteurId);
}