package sn.Voom.matchingservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sn.Voom.matchingservice.dto.ApiResponse;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String erreurs = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(" | "));
        return ResponseEntity.badRequest().body(ApiResponse.erreur("Validation : " + erreurs));
    }

    @ExceptionHandler({DemandeCourseNotFoundException.class, TrajetNotFoundException.class,
            AffectationNotFoundException.class, BagageNotFoundException.class, InvitationNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.erreur(ex.getMessage()));
    }

    @ExceptionHandler({CapaciteInsuffisanteException.class, DemandeDejAffecteeException.class})
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.erreur(ex.getMessage()));
    }

    @ExceptionHandler(AucuneDemandeDisponibleException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnprocessable(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ApiResponse.erreur(ex.getMessage()));
    }

    @ExceptionHandler(FirestoreOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleFirestore(FirestoreOperationException ex) {
        log.error("[Voom-Matching] Erreur Firestore", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.erreur(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("[Voom-Matching] Erreur inattendue", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.erreur("Erreur interne"));
    }
}
