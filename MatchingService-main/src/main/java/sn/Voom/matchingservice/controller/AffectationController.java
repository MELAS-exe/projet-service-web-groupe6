package sn.Voom.matchingservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.Voom.matchingservice.dto.ApiResponse;
import sn.Voom.matchingservice.dto.response.AffectationResponse;
import sn.Voom.matchingservice.service.AffectationService;

import java.util.List;
import java.util.Map;

/**
 * GET   /api/v1/affectations/{id}
 * GET   /api/v1/affectations?conducteurId=&statut=
 * POST  /api/v1/affectations/{id}/accepter   – conducteur accepte
 * POST  /api/v1/affectations/{id}/refuser    – conducteur refuse → récupération auto
 */
@RestController
@RequestMapping("/api/v1/affectations")
@RequiredArgsConstructor
public class AffectationController {

    private final AffectationService affectationService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AffectationResponse>> trouver(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(affectationService.trouverParId(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AffectationResponse>>> lister(
            @RequestParam(required = false) String conducteurId,
            @RequestParam(required = false) String statut) {
        List<AffectationResponse> liste = conducteurId != null
                ? affectationService.listerParConducteur(conducteurId)
                : affectationService.listerParStatut(statut != null ? statut : "PROPOSEE");
        return ResponseEntity.ok(ApiResponse.ok(liste));
    }

    /**
     * Le conducteur accepte l'affectation.
     * Corps : { "conducteurId": "...", "vehiculeId": "..." }
     */
    @PostMapping("/{id}/accepter")
    public ResponseEntity<ApiResponse<AffectationResponse>> accepter(
            @PathVariable String id,
            @RequestBody Map<String, String> corps) {
        String conducteurId = corps.get("conducteurId");
        String vehiculeId   = corps.get("vehiculeId");
        return ResponseEntity.ok(ApiResponse.ok(
                affectationService.accepter(id, conducteurId, vehiculeId),
                "Affectation acceptée – trajet programmé"));
    }

    /**
     * Le conducteur refuse l'affectation.
     * Corps : { "conducteurId": "..." }
     * → Déclenche automatiquement la récupération du groupe
     */
    @PostMapping("/{id}/refuser")
    public ResponseEntity<ApiResponse<AffectationResponse>> refuser(
            @PathVariable String id,
            @RequestBody Map<String, String> corps) {
        String conducteurId = corps.get("conducteurId");
        return ResponseEntity.ok(ApiResponse.ok(
                affectationService.refuser(id, conducteurId),
                "Affectation refusée – récupération du groupe en cours"));
    }
}
