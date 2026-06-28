package br.usp.lab.oo.planejador_feriado.recommendation.config;

public final class RecommendationLimits {

  public static final int MAX_RESULTS = 15;
  public static final int MAX_RECOMMENDATION_WINDOW_DAYS = 92;
  public static final int MAX_BEST_WINDOWS_PERIOD_DAYS = 400;
  public static final int MAX_EXPLICIT_CANDIDATES = 50;
  public static final int MAX_REGION_CANDIDATES = 60;
  public static final int MAX_TRAVELERS = 10;

  private RecommendationLimits() {}
}
