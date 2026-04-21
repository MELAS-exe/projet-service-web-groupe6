package sn.Voom.matchingservice.service;

import org.springframework.stereotype.Service;
import sn.Voom.matchingservice.entite.enums.RegionSenegal;

/**
 * Détecte automatiquement la région sénégalaise la plus proche
 * à partir de coordonnées GPS.
 *
 * Utilise la distance Haversine entre le point GPS et chaque chef-lieu.
 * Retourne la région dont le chef-lieu est le plus proche.
 */
@Service
public class RegionDetectionService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Détecte la région depuis des coordonnées GPS.
     */
    public RegionSenegal detecterRegion(double latitude, double longitude) {
        RegionSenegal plusProche = null;
        double distanceMin = Double.MAX_VALUE;

        for (RegionSenegal region : RegionSenegal.values()) {
            double distance = haversineKm(
                    latitude, longitude,
                    region.latitude, region.longitude);

            if (distance < distanceMin) {
                distanceMin = distance;
                plusProche  = region;
            }
        }

        return plusProche;
    }

    /**
     * Calcule la distance en km entre un point GPS et le chef-lieu d'une région.
     * Utilisé pour la surcharge hors-centre.
     */
    public double distanceAuCentre(double latitude, double longitude, RegionSenegal region) {
        return haversineKm(latitude, longitude, region.latitude, region.longitude);
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}