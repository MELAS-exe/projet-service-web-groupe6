package sn.Voom.matchingservice.entite;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import sn.Voom.matchingservice.entite.enums.StatutAffectation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "affectations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Affectation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "trajet_id", nullable = false)
    private UUID trajetId;

    @Column(name = "conducteur_id")
    private String conducteurId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "demandes_ids", columnDefinition = "text[]")
    private List<String> demandesIds;

    @Column(name = "revenus_estimes")
    private float revenusEstimes;

    @Column(name = "distance_totale_km")
    private float distanceTotaleKm;

    @Column(name = "date_affectation")
    private LocalDateTime dateAffectation;

    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutAffectation statut = StatutAffectation.PROPOSEE;

    @Column(name = "tentative_numero")
    private int tentativeNumero = 1;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "conducteurs_refus_ids", columnDefinition = "text[]")
    private List<String> conducteursRefusIds;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PrePersist
    void prePersist() {
        if (dateCreation == null)         dateCreation = LocalDateTime.now();
        if (dateModification == null)     dateModification = LocalDateTime.now();
        if (dateAffectation == null)      dateAffectation = LocalDateTime.now();
        if (conducteursRefusIds == null)  conducteursRefusIds = new ArrayList<>();
        if (demandesIds == null)          demandesIds = new ArrayList<>();
    }
}