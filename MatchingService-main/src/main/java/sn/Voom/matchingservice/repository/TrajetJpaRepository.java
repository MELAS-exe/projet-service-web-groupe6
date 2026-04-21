package sn.Voom.matchingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import sn.Voom.matchingservice.entite.Trajet;
import sn.Voom.matchingservice.entite.enums.StatutTrajet;

import java.util.List;
import java.util.UUID;

public interface TrajetJpaRepository extends JpaRepository<Trajet, UUID> {

    List<Trajet> findByConducteurId(String conducteurId);
    List<Trajet> findByStatut(StatutTrajet statut);

    @Modifying @Transactional
    @Query("UPDATE Trajet t SET t.statut = :statut WHERE t.id = :id")
    void updateStatut(@Param("id") UUID id, @Param("statut") StatutTrajet statut);

    @Modifying @Transactional
    @Query("UPDATE Trajet t SET t.conducteurId = :conducteurId, t.vehiculeId = :vehiculeId, t.statut = sn.Voom.matchingservice.entite.enums.StatutTrajet.PROGRAMME WHERE t.id = :id")
    void assignerConducteur(@Param("id") UUID id, @Param("conducteurId") String conducteurId, @Param("vehiculeId") String vehiculeId);
}