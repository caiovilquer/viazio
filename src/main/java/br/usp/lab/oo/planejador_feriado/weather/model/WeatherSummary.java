package br.usp.lab.oo.planejador_feriado.weather.model;

import java.time.LocalDate;

/**
 * Resumo climático esperado de um destino numa janela de datas, estimado a partir
 * da climatologia (médias do mesmo período no ano anterior, via Open-Meteo Archive).
 *
 * @param avgTempC          temperatura média diária (°C) na janela
 * @param avgDailyPrecipMm  precipitação média por dia (mm) na janela
 * @param sampledDays       número de dias com dados usados na média
 */
public record WeatherSummary(
        double avgTempC,
        double avgDailyPrecipMm,
        double rainyDayProbability,
        double tempStdDevC,
        int sampledDays,
        int sampledYears,
        WeatherSourceType sourceType,
        LocalDate referenceFrom,
        LocalDate referenceTo
) {
    public WeatherSummary(double avgTempC, double avgDailyPrecipMm, int sampledDays) {
        this(avgTempC, avgDailyPrecipMm, 0.0, 0.0, sampledDays, 1,
                WeatherSourceType.CLIMATOLOGY, null, null);
    }
}
