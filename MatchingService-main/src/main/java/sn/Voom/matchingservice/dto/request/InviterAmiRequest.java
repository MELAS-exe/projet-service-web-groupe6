package sn.Voom.matchingservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class InviterAmiRequest {

    @NotBlank(message = "L'identifiant du trajet est obligatoire")
    private String trajetId;

    @NotBlank(message = "L'identifiant de l'inviteur est obligatoire")
    private String inviteurId;

    @NotBlank(message = "L'identifiant de l'invité est obligatoire")
    private String inviteId;

    private String message;

    @Min(1) @Max(8)
    private int placesProposees = 1;

    @DecimalMin("0.0")
    private float tarifAmi = 0f;
}
