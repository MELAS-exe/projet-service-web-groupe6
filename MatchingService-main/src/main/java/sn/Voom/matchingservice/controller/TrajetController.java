package sn.Voom.matchingservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.Voom.matchingservice.dto.ApiResponse;
import sn.Voom.matchingservice.dto.finance.ResumeFinancier;
import sn.Voom.matchingservice.dto.response.DemandeCourseResponse;
import sn.Voom.matchingservice.dto.response.TrajetResponse;
import sn.Voom.matchingservice.service.TrajetService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GET   /api/v1/trajets/{id}
 * GET   /api/v1/trajets?conducteurId=&statut=
 * PATCH /api/v1/trajets/{id}/demarrer
 * PATCH /api/v1/trajets/{id}/terminer
 * GET   /api/v1/trajets/{id}/resume-financier   ← consommé par le service financier
 * GET   /api/v1/trajets/{id}/passagers          ← liste des passagers du trajet
 */
@RestController
@RequestMapping("/api/v1/trajets")
@RequiredArgsConstructor
public class TrajetController {

    private final TrajetService trajetService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrajetResponse>> trouver(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(trajetService.trouverParId(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TrajetResponse>>> lister(
            @RequestParam(required = false) String conducteurId,
            @RequestParam(required = false) String statut) {
        List<TrajetResponse> liste = conducteurId != null
                ? trajetService.listerParConducteur(conducteurId)
                : trajetService.listerParStatut(statut != null ? statut : "EN_ATTENTE_CONDUCTEUR");
        return ResponseEntity.ok(ApiResponse.ok(liste, liste.size() + " trajet(s)"));
    }

    @PatchMapping("/{id}/demarrer")
    public ResponseEntity<ApiResponse<TrajetResponse>> demarrer(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(
                trajetService.demarrer(id), "Trajet démarré"));
    }

    @PatchMapping("/{id}/terminer")
    public ResponseEntity<ApiResponse<TrajetResponse>> terminer(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(
                trajetService.terminer(id), "Trajet terminé"));
    }

    /**
     * Résumé financier complet — consommé par le service financier.
     * Disponible uniquement quand statut = TERMINEE.
     */
    @GetMapping("/{id}/resume-financier")
    public ResponseEntity<ApiResponse<ResumeFinancier>> resumeFinancier(
            @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(
                trajetService.getResumeFinancier(id),
                "Résumé financier disponible"));
    }

    /**
     * Liste des passagers d'un trajet — utile pour la finance et les notifications.
     */
    @GetMapping("/{id}/passagers")
    public ResponseEntity<ApiResponse<List<String>>> listerPassagers(
            @PathVariable String id) {
        List<String> passagerIds = trajetService.listerPassagers(id)
                .stream()
                .map(d -> d.getPassagerId())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(passagerIds,
                passagerIds.size() + " passager(s)"));
    }
}