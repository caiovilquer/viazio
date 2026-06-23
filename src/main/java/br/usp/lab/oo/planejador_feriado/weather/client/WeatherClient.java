package br.usp.lab.oo.planejador_feriado.weather.client;

import br.usp.lab.oo.planejador_feriado.weather.dto.OpenMeteoArchiveResponse;

import java.time.LocalDate;

/**
 * Abstrai o acesso a dados climáticos históricos, permitindo decorar a
 * implementação real (ex.: com cache) sem que {@code WeatherService} conheça o detalhe.
 */
public interface WeatherClient {

    OpenMeteoArchiveResponse getDailyClimate(double latitude, double longitude, LocalDate start, LocalDate end);
}
