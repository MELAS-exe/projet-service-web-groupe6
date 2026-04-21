package sn.Voom.matchingservice.entite;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import sn.Voom.matchingservice.entite.enums.StatutInvitation;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invitations_trajet")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationTrajet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "trajet_id", nullable = false)
    private  UUID trajetId;

    @Column(name = "inviteur_id", nullable = false)
    private String inviteurId;

    @Column(name = "invite_id", nullable = false)
    private String inviteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutInvitation statut = StatutInvitation.ENVOYEE;

    @Column(name = "date_invitation")
    private LocalDateTime dateInvitation;

    @Column(name = "date_expiration")
    private LocalDateTime dateExpiration;

    @Column(name = "date_reponse")
    private LocalDateTime dateReponse;

    @Column(name = "message")
    private String message;

    @Column(name = "places_proposees")
    private int placesProposees = 1;

    @Column(name = "tarif_ami")
    private float tarifAmi = 0f;

    @PrePersist
    void prePersist() {
        if (dateInvitation == null) dateInvitation = LocalDateTime.now();
        if (dateExpiration == null) dateExpiration = LocalDateTime.now().plusHours(24);
    }
}