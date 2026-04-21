package sn.Voom.matchingservice.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.CONFLICT)
public class DemandeDejAffecteeException extends RuntimeException {
    public DemandeDejAffecteeException(String id) { super("Demande déjà affectée : " + id); }
}
