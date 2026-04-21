package sn.Voom.matchingservice.entite.enums;

/**
 * Les 8 régions principales du Sénégal avec les coordonnées
 * de leur chef-lieu pour la détection automatique depuis les GPS.
 */
public enum RegionSenegal {

    DAKAR       ("Dakar",        14.693, -17.447),
    THIES       ("Thiès",        14.791, -16.924),
    DIOURBEL    ("Diourbel",     14.655, -16.232),
    KAOLACK     ("Kaolack",      14.152, -16.073),
    SAINT_LOUIS ("Saint-Louis",  16.017, -16.489),
    LOUGA       ("Louga",        15.617, -16.224),
    TAMBACOUNDA ("Tambacounda",  13.771, -13.667),
    ZIGUINCHOR  ("Ziguinchor",   12.567, -16.267);

    public final String nom;
    public final double latitude;
    public final double longitude;

    RegionSenegal(String nom, double latitude, double longitude) {
        this.nom       = nom;
        this.latitude  = latitude;
        this.longitude = longitude;
    }
}