package sn.Voom.matchingservice.dto.response;

import lombok.Builder;
import lombok.Data;
import sn.Voom.matchingservice.entite.enums.StatutInvitation;

import java.time.LocalDateTime;

@Data @Builder
public class InvitationTrajetResponse {
    private String id;
    private String trajetId;
    private String inviteurId;
    private String inviteId;
    private StatutInvitation statut;
    private LocalDateTime dateInvitation;
    private LocalDateTime dateExpiration;
    private String message;
    private int placesProposees;
    private float tarifAmi;
}
