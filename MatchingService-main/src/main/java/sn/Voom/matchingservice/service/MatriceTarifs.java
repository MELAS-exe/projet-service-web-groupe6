package sn.Voom.matchingservice.service;

import org.springframework.stereotype.Component;
import sn.Voom.matchingservice.entite.enums.RegionSenegal;

import java.util.HashMap;
import java.util.Map;

/**
 * Matrice des distances routières (km) et des prix de base (F CFA)
 * entre les 8 régions principales du Sénégal.
 *
 * Source : document "Matrices de Facturation — Sénégal"
 * Référence calibrage : Dakar ↔ Thiès = 70 km
 *   → α_voyage    = 71,4 F/km → 5 000 F CFA
 *   → α_livraison = 42,9 F/km → 3 000 F CFA
 */
@Component
public class MatriceTarifs {

    // ── Distances routières en km ─────────────────────────────────────────────
    private static final Map<String, Integer> DISTANCES = new HashMap<>();

    // ── Prix de base voyage (F CFA par place) ─────────────────────────────────
    private static final Map<String, Integer> PRIX_VOYAGE = new HashMap<>();

    // ── Prix de base livraison (F CFA par colis) ──────────────────────────────
    private static final Map<String, Integer> PRIX_LIVRAISON = new HashMap<>();

    static {
        // Distances routières (km) — symétriques
        dist("DAKAR",       "THIES",        70);
        dist("DAKAR",       "DIOURBEL",    145);
        dist("DAKAR",       "KAOLACK",     195);
        dist("DAKAR",       "SAINT_LOUIS", 270);
        dist("DAKAR",       "LOUGA",       200);
        dist("DAKAR",       "TAMBACOUNDA", 455);
        dist("DAKAR",       "ZIGUINCHOR",  490);
        dist("THIES",       "DIOURBEL",     80);
        dist("THIES",       "KAOLACK",     130);
        dist("THIES",       "SAINT_LOUIS", 235);
        dist("THIES",       "LOUGA",       145);
        dist("THIES",       "TAMBACOUNDA", 390);
        dist("THIES",       "ZIGUINCHOR",  430);
        dist("DIOURBEL",    "KAOLACK",     110);
        dist("DIOURBEL",    "SAINT_LOUIS", 300);
        dist("DIOURBEL",    "LOUGA",       190);
        dist("DIOURBEL",    "TAMBACOUNDA", 345);
        dist("DIOURBEL",    "ZIGUINCHOR",  430);
        dist("KAOLACK",     "SAINT_LOUIS", 380);
        dist("KAOLACK",     "LOUGA",       310);
        dist("KAOLACK",     "TAMBACOUNDA", 265);
        dist("KAOLACK",     "ZIGUINCHOR",  310);
        dist("SAINT_LOUIS", "LOUGA",       130);
        dist("SAINT_LOUIS", "TAMBACOUNDA", 590);
        dist("SAINT_LOUIS", "ZIGUINCHOR",  690);
        dist("LOUGA",       "TAMBACOUNDA", 480);
        dist("LOUGA",       "ZIGUINCHOR",  590);
        dist("TAMBACOUNDA", "ZIGUINCHOR",  300);

        // Prix de base voyage (F CFA par place)
        voyage("DAKAR",       "THIES",         5000);
        voyage("DAKAR",       "DIOURBEL",      10400);
        voyage("DAKAR",       "KAOLACK",       13900);
        voyage("DAKAR",       "SAINT_LOUIS",   19300);
        voyage("DAKAR",       "LOUGA",         14300);
        voyage("DAKAR",       "TAMBACOUNDA",   32500);
        voyage("DAKAR",       "ZIGUINCHOR",    35000);
        voyage("THIES",       "DIOURBEL",       5700);
        voyage("THIES",       "KAOLACK",        9300);
        voyage("THIES",       "SAINT_LOUIS",   16800);
        voyage("THIES",       "LOUGA",         10400);
        voyage("THIES",       "TAMBACOUNDA",   27800);
        voyage("THIES",       "ZIGUINCHOR",    30700);
        voyage("DIOURBEL",    "KAOLACK",        7900);
        voyage("DIOURBEL",    "SAINT_LOUIS",   21400);
        voyage("DIOURBEL",    "LOUGA",         13600);
        voyage("DIOURBEL",    "TAMBACOUNDA",   24600);
        voyage("DIOURBEL",    "ZIGUINCHOR",    30700);
        voyage("KAOLACK",     "SAINT_LOUIS",   27100);
        voyage("KAOLACK",     "LOUGA",         22100);
        voyage("KAOLACK",     "TAMBACOUNDA",   18900);
        voyage("KAOLACK",     "ZIGUINCHOR",    22100);
        voyage("SAINT_LOUIS", "LOUGA",          9300);
        voyage("SAINT_LOUIS", "TAMBACOUNDA",   42100);
        voyage("SAINT_LOUIS", "ZIGUINCHOR",    49300);
        voyage("LOUGA",       "TAMBACOUNDA",   34300);
        voyage("LOUGA",       "ZIGUINCHOR",    42100);
        voyage("TAMBACOUNDA", "ZIGUINCHOR",    21400);

        // Prix de base livraison (F CFA par colis)
        livraison("DAKAR",       "THIES",         3000);
        livraison("DAKAR",       "DIOURBEL",       6200);
        livraison("DAKAR",       "KAOLACK",        8400);
        livraison("DAKAR",       "SAINT_LOUIS",   11600);
        livraison("DAKAR",       "LOUGA",          8600);
        livraison("DAKAR",       "TAMBACOUNDA",   19500);
        livraison("DAKAR",       "ZIGUINCHOR",    21000);
        livraison("THIES",       "DIOURBEL",       3400);
        livraison("THIES",       "KAOLACK",        5600);
        livraison("THIES",       "SAINT_LOUIS",   10100);
        livraison("THIES",       "LOUGA",          6200);
        livraison("THIES",       "TAMBACOUNDA",   16700);
        livraison("THIES",       "ZIGUINCHOR",    18400);
        livraison("DIOURBEL",    "KAOLACK",        4700);
        livraison("DIOURBEL",    "SAINT_LOUIS",   12900);
        livraison("DIOURBEL",    "LOUGA",          8200);
        livraison("DIOURBEL",    "TAMBACOUNDA",   14800);
        livraison("DIOURBEL",    "ZIGUINCHOR",    18400);
        livraison("KAOLACK",     "SAINT_LOUIS",   16300);
        livraison("KAOLACK",     "LOUGA",         13300);
        livraison("KAOLACK",     "TAMBACOUNDA",   11400);
        livraison("KAOLACK",     "ZIGUINCHOR",    13300);
        livraison("SAINT_LOUIS", "LOUGA",          5600);
        livraison("SAINT_LOUIS", "TAMBACOUNDA",   25300);
        livraison("SAINT_LOUIS", "ZIGUINCHOR",    29600);
        livraison("LOUGA",       "TAMBACOUNDA",   20600);
        livraison("LOUGA",       "ZIGUINCHOR",    25300);
        livraison("TAMBACOUNDA", "ZIGUINCHOR",    12900);
    }

    // ── Méthodes publiques ────────────────────────────────────────────────────

    public int getPrixVoyage(RegionSenegal regionA, RegionSenegal regionB) {
        if (regionA == regionB) return 0;
        return PRIX_VOYAGE.getOrDefault(cle(regionA, regionB), -1);
    }

    public int getPrixLivraison(RegionSenegal regionA, RegionSenegal regionB) {
        if (regionA == regionB) return 0;
        return PRIX_LIVRAISON.getOrDefault(cle(regionA, regionB), -1);
    }

    public int getDistance(RegionSenegal regionA, RegionSenegal regionB) {
        if (regionA == regionB) return 0;
        return DISTANCES.getOrDefault(cle(regionA, regionB), -1);
    }

    // ── Helpers statiques ─────────────────────────────────────────────────────

    private static String cle(RegionSenegal a, RegionSenegal b) {
        // Clé symétrique : toujours dans l'ordre alphabétique
        String na = a.name();
        String nb = b.name();
        return na.compareTo(nb) <= 0 ? na + "_" + nb : nb + "_" + na;
    }

    private static void dist(String a, String b, int km) {
        String key = a.compareTo(b) <= 0 ? a + "_" + b : b + "_" + a;
        DISTANCES.put(key, km);
    }

    private static void voyage(String a, String b, int prix) {
        String key = a.compareTo(b) <= 0 ? a + "_" + b : b + "_" + a;
        PRIX_VOYAGE.put(key, prix);
    }

    private static void livraison(String a, String b, int prix) {
        String key = a.compareTo(b) <= 0 ? a + "_" + b : b + "_" + a;
        PRIX_LIVRAISON.put(key, prix);
    }
}