package sn.Voom.matchingservice.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FirestoreOperationException extends RuntimeException {
    public FirestoreOperationException(String op, Throwable cause) {
        super("Erreur Firestore lors de : " + op, cause);
    }
}
