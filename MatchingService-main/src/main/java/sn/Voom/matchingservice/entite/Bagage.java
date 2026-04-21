package sn.Voom.matchingservice.entite;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import sn.Voom.matchingservice.entite.enums.StatutBagage;
import sn.Voom.matchingservice.entite.enums.TypeBagage;
import sn.Voom.matchingservice.entite.enums.TypeOperation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bagages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bagage {

    public static float poidsEstimePar(TypeBagage type) {
        return switch (type) {
            case MINI  -> 1f;
            case PETIT -> 3.5f;
            case MOYEN -> 10f;
            case GRAND -> 22f;
            case MEGA  -> 40f;
        };
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "demande_id", nullable = false)
    private UUID demandeId;

    @Column(name = "expediteur_id", nullable = false)
    private String expediteurId;

    @Column(name = "destinataire_id")
    private String destinataireId;

    @Column(name = "description", nullable = false)
    private String description;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "types", columnDefinition = "text[]")  // type_bagage[] → text[]
    private List<String> types;  // List<TypeBagage> → List<String>

    @Column(name = "poids_kg", nullable = false)
    private float poidsKg;

    @Column(name = "fragile")
    private boolean fragile = false;

    @Column(name = "entre_amis")
    private boolean entreAmis = false;

    @Column(name = "ami_destinataire_id")
    private String amiDestinataireId;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutBagage statut = StatutBagage.EN_ATTENTE;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_confirmation_reception")
    private LocalDateTime dateConfirmationReception;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_operation")
    private TypeOperation typeOperation = TypeOperation.BAGAGE_PERSONNEL;

    @Column(name = "prix_calcule")
    private float prixCalcule = 0f;

    @PrePersist
    void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
    }
}