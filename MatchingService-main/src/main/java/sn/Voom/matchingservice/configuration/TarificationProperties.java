package sn.Voom.matchingservice.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tarification")
public class TarificationProperties {

    // ── Taux kilométriques ────────────────────────────────────────────────────
    private double alphaVoyage     = 71.4;
    private double alphaLivraison  = 42.9;

    // ── Surcharge hors-centre ─────────────────────────────────────────────────
    private double alphaHcVoyage    = 50.0;
    private double alphaHcLivraison = 35.0;

    // ── Bagages voyage ────────────────────────────────────────────────────────
    private double pMiniVoyage   = 0.0;
    private double pPetitVoyage  = 0.0;
    private double pMoyenVoyage  = 1000.0;
    private double pGrandVoyage  = 2000.0;

    // ── Bagages livraison ─────────────────────────────────────────────────────
    private double pMiniLivraison   = 500.0;
    private double pPetitLivraison  = 1000.0;
    private double pMoyenLivraison  = 2000.0;
    private double pGrandLivraison  = 3500.0;

    // ── Commission Voom ───────────────────────────────────────────────────────
    private double tauxCommission = 0.15;
}