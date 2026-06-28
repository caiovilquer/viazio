package br.usp.lab.oo.planejador_feriado.common.geo;

/**
 * Utilitário de cálculo geográfico. Usa a fórmula de Haversine para estimar a
 * distância "em linha reta" (great-circle) entre dois pontos da superfície
 * terrestre, suficiente para comparar proximidade de destinos.
 */
public final class GeoCalculator {

  private static final double EARTH_RADIUS_KM = 6371.0;

  private GeoCalculator() {}

  /** Distância great-circle em quilômetros entre dois pares (lat, lon) em graus. */
  public static double haversineKm(
    double lat1,
    double lon1,
    double lat2,
    double lon2
  ) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);

    double a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(Math.toRadians(lat1)) *
        Math.cos(Math.toRadians(lat2)) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS_KM * c;
  }
}
