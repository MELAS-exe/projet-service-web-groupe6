package sn.Voom.matchingservice.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TrajetNotFoundException extends RuntimeException {
    public TrajetNotFoundException(String id) { super("Trajet introuvable : " + id); }
}
