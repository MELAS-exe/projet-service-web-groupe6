package sn.Voom.matchingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import sn.Voom.matchingservice.entite.Bagage;
import sn.Voom.matchingservice.entite.enums.StatutBagage;

import java.util.List;
import java.util.UUID;

public interface BagageJpaRepository extends JpaRepository<Bagage, UUID> {

    List<Bagage> findByDemandeId(UUID demandeId);

    // Pour le résumé financier — tous les bagages de plusieurs demandes
    @Query("SELECT b FROM Bagage b WHERE b.demandeId IN :demandeIds")
    List<Bagage> findByDemandeIdIn(@Param("demandeIds") List<UUID> demandeIds);

    // expediteurId est VARCHAR → String
    List<Bagage> findByExpediteurId(String expediteurId);

    @Modifying @Transactional
    @Query("UPDATE Bagage b SET b.statut = :statut WHERE b.id = :id")
    void updateStatut(@Param("id") UUID id, @Param("statut") StatutBagage statut);
}