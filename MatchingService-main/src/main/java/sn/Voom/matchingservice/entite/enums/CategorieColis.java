package sn.Voom.matchingservice.entite.enums;

/**
 * Catégorie d'un colis pour la facturation livraison.
 *
 * Grille tarifaire (livraison) :
 *   MINI  → < 5 kg   → 500 FCFA
 *   PETIT → < 15 kg  → 1 000 FCFA
 *   MOYEN → < 30 kg  → 2 000 FCFA
 *   GRAND → < 50 kg  → 3 500 FCFA
 */
public enum CategorieColis {
    MINI,   // Sac à dos, cabas < 5 kg
    PETIT,  // Valise cabine, carton < 15 kg
    MOYEN,  // Valise 24", carton < 30 kg
    GRAND   // Grande valise, sac < 50 kg
}