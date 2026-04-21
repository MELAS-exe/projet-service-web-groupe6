package sn.Voom.matchingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.Voom.matchingservice.dto.ApiResponse;
import sn.Voom.matchingservice.dto.request.CreerDemandeCourseRequest;
import sn.Voom.matchingservice.dto.response.BagageResponse;
import sn.Voom.matchingservice.dto.response.DemandeCourseResponse;
import sn.Voom.matchingservice.service.BagageService;
import sn.Voom.matchingservice.service.DemandeCourseService;

import java.util.List;

/**
 * POST   /api/v1/demandes              – créer + auto-matching
 * GET    /api/v1/demandes/{id}         – détail
 * GET    /api/v1/demandes?passagerId=  – historique passager
 * GET    /api/v1/demandes/en-attente   – pool en attente
 * PATCH  /api/v1/demandes/{id}/annuler – annuler
 * GET    /api/v1/demandes/{id}/bagages – bagages de la demande
 */
@RestController
@RequestMapping("/api/v1/demandes")
@RequiredArgsConstructor
public class    DemandeCourseController {

    private final DemandeCourseService demandeService;
    private final BagageService bagageService;

    @PostMapping
    public ResponseEntity<ApiResponse<DemandeCourseResponse>> creer(
            @Valid @RequestBody CreerDemandeCourseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(demandeService.creer(req), "Demande créée et matching déclenché"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DemandeCourseResponse>> trouver(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(demandeService.trouverParId(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DemandeCourseResponse>>> lister(
            @RequestParam(required = false) String passagerId) {
        List<DemandeCourseResponse> liste = passagerId != null
                ? demandeService.listerParPassager(passagerId)
                : demandeService.listerEnAttente();
        return ResponseEntity.ok(ApiResponse.ok(liste, liste.size() + " demande(s)"));
    }

    @GetMapping("/en-attente")
    public ResponseEntity<ApiResponse<List<DemandeCourseResponse>>> listerEnAttente() {
        List<DemandeCourseResponse> liste = demandeService.listerEnAttente();
        return ResponseEntity.ok(ApiResponse.ok(liste, liste.size() + " demande(s) en attente"));
    }

    @PatchMapping("/{id}/annuler")
    public ResponseEntity<ApiResponse<DemandeCourseResponse>> annuler(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(demandeService.annuler(id), "Demande annulée"));
    }

    @GetMapping("/{id}/bagages")
    public ResponseEntity<ApiResponse<List<BagageResponse>>> listerBagages(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(bagageService.listerParDemande(id)));
    }
}
