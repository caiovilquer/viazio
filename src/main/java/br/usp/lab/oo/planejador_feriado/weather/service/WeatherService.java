package br.usp.lab.oo.planejador_feriado.weather.service;

import br.usp.lab.oo.planejador_feriado.weather.client.WeatherClient;
import br.usp.lab.oo.planejador_feriado.weather.dto.OpenMeteoArchiveResponse;
import br.usp.lab.oo.planejador_feriado.weather.model.WeatherSummary;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Estima o clima esperado de um destino numa janela futura usando climatologia:
 * consulta o mesmo período do ano anterior (Open-Meteo Archive) como proxy, já que
 * previsões de verdade só existem para ~16 dias à frente.
 */
@Service
public class WeatherService {

    private static final int CLIMATOLOGY_YEARS_BACK = 1;

    private final WeatherClient client;

    public WeatherService(WeatherClient client) {
        this.client = client;
    }

    public Optional<WeatherSummary> getClimateForWindow(double latitude, double longitude, LocalDate from, LocalDate to) {
        LocalDate start = from.minusYears(CLIMATOLOGY_YEARS_BACK);
        LocalDate end = to.minusYears(CLIMATOLOGY_YEARS_BACK);

        OpenMeteoArchiveResponse response = client.getDailyClimate(latitude, longitude, start, end);
        if (response == null || response.daily() == null) {
            return Optional.empty();
        }

        OpenMeteoArchiveResponse.Daily daily = response.daily();
        List<Double> maxTemps = daily.temperature_2m_max();
        List<Double> minTemps = daily.temperature_2m_min();
        List<Double> precip = daily.precipitation_sum();

        if (maxTemps == null || minTemps == null || maxTemps.isEmpty()) {
            return Optional.empty();
        }

        double tempSum = 0.0;
        int tempDays = 0;
        int size = Math.min(maxTemps.size(), minTemps.size());
        for (int i = 0; i < size; i++) {
            Double max = maxTemps.get(i);
            Double min = minTemps.get(i);
            if (max != null && min != null) {
                tempSum += (max + min) / 2.0;
                tempDays++;
            }
        }

        if (tempDays == 0) {
            return Optional.empty();
        }

        double precipSum = 0.0;
        int precipDays = 0;
        if (precip != null) {
            for (Double value : precip) {
                if (value != null) {
                    precipSum += value;
                    precipDays++;
                }
            }
        }

        double avgTemp = tempSum / tempDays;
        double avgPrecip = precipDays > 0 ? precipSum / precipDays : 0.0;
        return Optional.of(new WeatherSummary(avgTemp, avgPrecip, tempDays));
    }
}
