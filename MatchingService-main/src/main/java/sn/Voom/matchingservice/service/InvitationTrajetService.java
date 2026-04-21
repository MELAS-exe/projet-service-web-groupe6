package sn.Voom.matchingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sn.Voom.matchingservice.dto.request.InviterAmiRequest;
import sn.Voom.matchingservice.dto.response.InvitationTrajetResponse;
import sn.Voom.matchingservice.entite.InvitationTrajet;
import sn.Voom.matchingservice.entite.Trajet;
import sn.Voom.matchingservice.entite.enums.StatutInvitation;
import sn.Voom.matchingservice.entite.enums.StatutTrajet;
import sn.Voom.matchingservice.exception.InvitationNotFoundException;
import sn.Voom.matchingservice.exception.TrajetNotFoundException;
import sn.Voom.matchingservice.repository.InvitationTrajetRepository;
import sn.Voom.matchingservice.repository.TrajetRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationTrajetService {

    private final InvitationTrajetRepository invitationRepo;
    private final TrajetRepository trajetRepo;

    public InvitationTrajetResponse inviter(InviterAmiRequest req) {
        Trajet trajet = trajetRepo.trouverParId(req.getTrajetId())
                .orElseThrow(() -> new TrajetNotFoundException(req.getTrajetId()));

        if (!trajet.isReservationAmisAutorisee()) {
            throw new IllegalStateException("Ce trajet n'autorise pas l'invitation d'amis");
        }
        if (trajet.getStatut() == StatutTrajet.TERMINE || trajet.getStatut() == StatutTrajet.ANNULE) {
            throw new IllegalStateException("Impossible d'inviter sur un trajet terminé ou annulé");
        }

        InvitationTrajet invitation = InvitationTrajet.builder()
                .trajetId(UUID.fromString(req.getTrajetId()))
                .inviteurId(req.getInviteurId())
                .inviteId(req.getInviteId())
                .statut(StatutInvitation.ENVOYEE)
                .dateInvitation(LocalDateTime.now())
                .dateExpiration(LocalDateTime.now().plusHours(24))
                .message(req.getMessage())
                .placesProposees(req.getPlacesProposees())
                .tarifAmi(req.getTarifAmi())
                .build();

        invitationRepo.sauvegarder(invitation);
        log.info("[Invitation] {} → {}", req.getInviteurId(), req.getInviteId());
        return versResponse(invitation);
    }

    public InvitationTrajetResponse accepter(String id) {
        InvitationTrajet inv = trouverOuLever(id);
        if (inv.getStatut() != StatutInvitation.ENVOYEE) {
            throw new IllegalStateException("L'invitation n'est plus en attente");
        }
        invitationRepo.mettreAJourStatut(id, StatutInvitation.ACCEPTEE);
        inv.setStatut(StatutInvitation.ACCEPTEE);
        return versResponse(inv);
    }

    public InvitationTrajetResponse refuser(String id) {
        InvitationTrajet inv = trouverOuLever(id);
        invitationRepo.mettreAJourStatut(id, StatutInvitation.REFUSEE);
        inv.setStatut(StatutInvitation.REFUSEE);
        return versResponse(inv);
    }

    public InvitationTrajetResponse annuler(String id) {
        InvitationTrajet inv = trouverOuLever(id);
        invitationRepo.mettreAJourStatut(id, StatutInvitation.ANNULEE);
        inv.setStatut(StatutInvitation.ANNULEE);
        return versResponse(inv);
    }

    public List<InvitationTrajetResponse> listerParInvite(String inviteId) {
        return invitationRepo.trouverParInvite(inviteId).stream()
                .map(this::versResponse).collect(Collectors.toList());
    }

    public List<InvitationTrajetResponse> listerParTrajet(String trajetId) {
        return invitationRepo.trouverParTrajet(trajetId).stream()
                .map(this::versResponse).collect(Collectors.toList());
    }

    private InvitationTrajet trouverOuLever(String id) {
        return invitationRepo.trouverParId(id)
                .orElseThrow(() -> new InvitationNotFoundException(id));
    }

    public InvitationTrajetResponse versResponse(InvitationTrajet i) {
        return InvitationTrajetResponse.builder()
                .id(String.valueOf(i.getId()))
                .trajetId(String.valueOf(i.getTrajetId()))
                .inviteurId(i.getInviteurId())
                .inviteId(i.getInviteId())
                .statut(i.getStatut())
                .dateInvitation(i.getDateInvitation())
                .dateExpiration(i.getDateExpiration())
                .message(i.getMessage())
                .placesProposees(i.getPlacesProposees())
                .tarifAmi(i.getTarifAmi())
                .build();
    }
}
