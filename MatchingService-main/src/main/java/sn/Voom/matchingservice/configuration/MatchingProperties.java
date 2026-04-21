package sn.Voom.matchingservice.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "matching")
public class MatchingProperties {

    // ── Matching de base ──────────────────────────────────────────────────────
    private double rayonKmDefaut = 3.0;
    private int toleranceMinutesDefaut = 20;
    private int capaciteVehiculeDefaut = 4;

    // ── Seuils batch ──────────────────────────────────────────────────────────
    private int seuilBatch = 3;
    private int seuilBatchImminent = 2;
    private int seuilBatchUrgent = 1;

    // ── Fenêtre temporelle ────────────────────────────────────────────────────
    private int fenetreJoursAvance = 7;

    // ── Expiration ────────────────────────────────────────────────────────────
    private int expirationAffectationMinutes = 15;
    private int delaiAnnulationOrphelineMinutes = 30;

    // ── Pondérations scoring ──────────────────────────────────────────────────
    private double poidsGeo    = 0.35;
    private double poidsTemps  = 0.35;
    private double poidsBagage = 0.20;
    private double poidsType   = 0.10;

    // ── Optimisation continue ─────────────────────────────────────────────────
    /** Délai max avant de forcer le matching même si groupe incomplet */
    private int delaiAttenteOptimisationMinutes = 15;

    /** Scheduler actif seulement entre ces heures (économie Cloud) */
    private int schedulerActifHeuresDebut = 6;
    private int schedulerActifHeuresFin   = 22;

    /** Intervalle scheduler en période creuse (peu de demandes) */
    private long intervalleSchedulerCreuxMs = 300000L; // 5 min

    /** Intervalle scheduler en période de pic (beaucoup de demandes) */
    private long intervalleSchedulerPicMs = 60000L; // 1 min

    /** Seuil de demandes en attente pour considérer que c'est un pic */
    private int seuilPicDemandes = 5;
}