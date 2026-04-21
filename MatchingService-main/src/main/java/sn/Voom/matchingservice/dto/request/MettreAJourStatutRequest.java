package sn.Voom.matchingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MettreAJourStatutRequest {
    @NotBlank(message = "Le statut est obligatoire")
    private String statut;
}
