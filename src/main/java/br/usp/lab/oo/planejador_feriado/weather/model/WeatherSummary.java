package br.usp.lab.oo.planejador_feriado.weather.model;

/**
 * Resumo climático esperado de um destino numa janela de datas, estimado a partir
 * da climatologia (médias do mesmo período no ano anterior, via Open-Meteo Archive).
 *
 * @param avgTempC          temperatura média diária (°C) na janela
 * @param avgDailyPrecipMm  precipitação média por dia (mm) na janela
 * @param sampledDays       número de dias com dados usados na média
 */
public record WeatherSummary(double avgTempC, double avgDailyPrecipMm, int sampledDays) {
}
