package sn.Voom.matchingservice.entite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coordonnees {
    private double latitude;
    private double longitude;
    private String adresse;
}
