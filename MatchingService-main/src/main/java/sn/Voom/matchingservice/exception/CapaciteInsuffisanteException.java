package sn.Voom.matchingservice.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.CONFLICT)
public class CapaciteInsuffisanteException extends RuntimeException {
    public CapaciteInsuffisanteException(String msg) { super(msg); }
}
