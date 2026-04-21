package sn.Voom.matchingservice.entite.enums;

public enum StatutDemandeEnvoi {
    EN_ATTENTE,           // créée, pas encore matchée
    EN_COURS_MATCHING,    // en cours de traitement batch
    AFFECTEE,             // assignée à un trajet
    EN_COURS,             // trajet démarré
    LIVREE,               // colis livré, destinataire a confirmé
    ANNULEE               // annulée par l'expéditeur
}