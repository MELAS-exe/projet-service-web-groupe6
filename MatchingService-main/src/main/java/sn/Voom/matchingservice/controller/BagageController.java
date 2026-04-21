package sn.Voom.matchingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.Voom.matchingservice.dto.ApiResponse;
import sn.Voom.matchingservice.dto.request.CreerBagageRequest;
import sn.Voom.matchingservice.dto.response.BagageResponse;
import sn.Voom.matchingservice.service.BagageService;

/**
 * POST  /api/v1/bagages                        – déclarer un bagage
 * GET   /api/v1/bagages/{id}                   – détail
 * PATCH /api/v1/bagages/{id}/confirmer-reception – destinataire confirme
 */
@RestController
@RequestMapping("/api/v1/bagages")
@RequiredArgsConstructor
public class BagageController {

    private final BagageService bagageService;

    @PostMapping
    public ResponseEntity<ApiResponse<BagageResponse>> creer(@Valid @RequestBody CreerBagageRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(bagageService.creer(req), "Bagage déclaré"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BagageResponse>> trouver(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(bagageService.trouverParId(id)));
    }

    @PatchMapping("/{id}/confirmer-reception")
    public ResponseEntity<ApiResponse<BagageResponse>> confirmerReception(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(bagageService.confirmerReception(id), "Réception confirmée"));
    }
}
