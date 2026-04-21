package sn.Voom.matchingservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import sn.Voom.matchingservice.entite.enums.TypeBagage;

import java.util.List;

@Data
public class CreerBagageRequest {

    @NotBlank(message = "L'ID de la demande est obligatoire")
    private String demandeId;

    @NotBlank(message = "L'expéditeur est obligatoire")
    private String expediteurId;

    private String destinataireId;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotEmpty(message = "Au moins un type de bagage est requis")
    private List<TypeBagage> types;

    @DecimalMin(value = "0.1", message = "Le poids doit être supérieur à 0")
    @DecimalMax(value = "100.0", message = "Poids maximum 100 kg")
    private float poidsKg;

    private boolean fragile = false;
    private boolean entreAmis = false;
    private String amiDestinataireId;
}
