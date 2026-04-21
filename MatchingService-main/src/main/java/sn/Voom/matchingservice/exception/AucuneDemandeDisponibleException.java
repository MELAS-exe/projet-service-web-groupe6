package sn.Voom.matchingservice.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class AucuneDemandeDisponibleException extends RuntimeException {
    public AucuneDemandeDisponibleException() { super("Aucune demande disponible pour le matching"); }
}
