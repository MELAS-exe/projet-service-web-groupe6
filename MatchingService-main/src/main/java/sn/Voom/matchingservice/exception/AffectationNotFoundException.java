package sn.Voom.matchingservice.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AffectationNotFoundException extends RuntimeException {
    public AffectationNotFoundException(String id) { super("Affectation introuvable : " + id); }
}
