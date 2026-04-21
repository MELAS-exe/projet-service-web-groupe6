package sn.Voom.matchingservice.exception;

public class DemandeEnvoiNotFoundException extends RuntimeException {
    public DemandeEnvoiNotFoundException(String id) {
        super("Demande d'envoi introuvable : " + id);
    }
}