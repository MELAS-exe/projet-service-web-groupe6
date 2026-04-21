package sn.Voom.matchingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.Voom.matchingservice.dto.ApiResponse;
import sn.Voom.matchingservice.dto.request.InviterAmiRequest;
import sn.Voom.matchingservice.dto.response.InvitationTrajetResponse;
import sn.Voom.matchingservice.service.InvitationTrajetService;

import java.util.List;

/**
 * POST  /api/v1/invitations                   – inviter un ami à un trajet
 * GET   /api/v1/invitations/{id}              – détail
 * GET   /api/v1/invitations?inviteId=         – mes invitations reçues
 * PATCH /api/v1/invitations/{id}/accepter
 * PATCH /api/v1/invitations/{id}/refuser
 * PATCH /api/v1/invitations/{id}/annuler
 */
@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationTrajetController {

    private final InvitationTrajetService invitationService;

    @Operation(description = "")
    @PostMapping
    public ResponseEntity<ApiResponse<InvitationTrajetResponse>> inviter(
            @Valid @RequestBody InviterAmiRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(invitationService.inviter(req), "Invitation envoyée"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvitationTrajetResponse>>> lister(
            @RequestParam(required = false) String inviteId,
            @RequestParam(required = false) String trajetId) {
        List<InvitationTrajetResponse> liste = inviteId != null
                ? invitationService.listerParInvite(inviteId)
                : invitationService.listerParTrajet(trajetId);
        return ResponseEntity.ok(ApiResponse.ok(liste));
    }

    @PatchMapping("/{id}/accepter")
    public ResponseEntity<ApiResponse<InvitationTrajetResponse>> accepter(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(invitationService.accepter(id), "Invitation acceptée"));
    }

    @PatchMapping("/{id}/refuser")
    public ResponseEntity<ApiResponse<InvitationTrajetResponse>> refuser(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(invitationService.refuser(id), "Invitation refusée"));
    }

    @PatchMapping("/{id}/annuler")
    public ResponseEntity<ApiResponse<InvitationTrajetResponse>> annuler(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(invitationService.annuler(id), "Invitation annulée"));
    }
}
