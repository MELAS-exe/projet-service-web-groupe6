package sn.Voom.matchingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.Voom.matchingservice.dto.ApiResponse;
import sn.Voom.matchingservice.dto.request.CreerDemandeEnvoiRequest;
import sn.Voom.matchingservice.dto.response.DemandeEnvoiResponse;
import sn.Voom.matchingservice.service.DemandeEnvoiService;

import java.util.List;
import java.util.Map;

/**
 * POST   /api/v1/envois                           – créer une demande d'envoi colis
 * GET    /api/v1/envois/{id}                      – détail
 * GET    /api/v1/envois?expediteurId=             – colis envoyés
 * GET    /api/v1/envois?destinataireId=           – colis à recevoir
 * GET    /api/v1/envois?trajetId=                 – colis d'un trajet
 * PATCH  /api/v1/envois/{id}/confirmer-livraison  – destinataire confirme avec code
 * PATCH  /api/v1/envois/{id}/annuler              – annuler
 */
@RestController
@RequestMapping("/api/v1/envois")
@RequiredArgsConstructor
public class DemandeEnvoiController {

    private final DemandeEnvoiService envoiService;

    @PostMapping
    public ResponseEntity<ApiResponse<DemandeEnvoiResponse>> creer(
            @Valid @RequestBody CreerDemandeEnvoiRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(envoiService.creer(req), "Demande d'envoi créée"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DemandeEnvoiResponse>> trouver(
            @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(envoiService.trouverParId(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DemandeEnvoiResponse>>> lister(
            @RequestParam(required = false) String expediteurId,
            @RequestParam(required = false) String destinataireId,
            @RequestParam(required = false) String trajetId) {

        List<DemandeEnvoiResponse> liste;

        if (expediteurId != null) {
            liste = envoiService.listerParExpediteur(expediteurId);
        } else if (destinataireId != null) {
            liste = envoiService.listerParDestinataire(destinataireId);
        } else if (trajetId != null) {
            liste = envoiService.listerParTrajet(trajetId);
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.erreur("Paramètre requis : expediteurId, destinataireId ou trajetId"));
        }

        return ResponseEntity.ok(ApiResponse.ok(liste, liste.size() + " envoi(s)"));
    }

    @Operation(
            /**
             * Le destinataire confirme la réception avec le code à 4 chiffres.
             * Corps : { "codeConfirmation": "1234" }
            */
    )
    @PatchMapping("/{id}/confirmer-livraison")
    public ResponseEntity<ApiResponse<DemandeEnvoiResponse>> confirmerLivraison(
            @PathVariable String id,
            @RequestBody Map<String, String> corps) {
        String code = corps.get("codeConfirmation");
        return ResponseEntity.ok(ApiResponse.ok(
                envoiService.confirmerLivraison(id, code),
                "Livraison confirmée"));
    }

    @PatchMapping("/{id}/annuler")
    public ResponseEntity<ApiResponse<DemandeEnvoiResponse>> annuler(
            @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(
                envoiService.annuler(id), "Envoi annulé"));
    }
}