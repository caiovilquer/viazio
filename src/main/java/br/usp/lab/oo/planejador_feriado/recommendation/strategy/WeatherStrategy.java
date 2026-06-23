package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import br.usp.lab.oo.planejador_feriado.weather.model.WeatherSummary;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Avalia o clima esperado no destino durante a janela (climatologia do ano anterior).
 * Premia temperatura na faixa confortável (18–28 °C) e pouca chuva. Sem dado de clima,
 * o critério fica indisponível e não penaliza o destino.
 */
@Component
public class WeatherStrategy implements ScoringStrategy {

    private static final double COMFORT_MIN = 18.0;
    private static final double COMFORT_MAX = 28.0;

    @Override
    public Criterion criterion() {
        return Criterion.WEATHER;
    }

    @Override
    public ScoreEntry evaluate(RecommendationContext context) {
        WeatherSummary weather = context.weather();
        if (weather == null) {
            return ScoreEntry.unavailable(criterion(), "Clima indisponível para o destino");
        }

        double tempScore = temperatureScore(weather.avgTempC());
        double precipScore = Math.max(0.0, 100.0 - weather.avgDailyPrecipMm() * 12.0);
        double score = 0.7 * tempScore + 0.3 * precipScore;

        String justification = String.format(Locale.ROOT,
                "%s: ~%.0f°C e %s",
                qualitative(score),
                weather.avgTempC(),
                describeRain(weather.avgDailyPrecipMm()));
        return ScoreEntry.of(criterion(), score, justification);
    }

    private double temperatureScore(double avgTemp) {
        double deviation = 0.0;
        if (avgTemp < COMFORT_MIN) {
            deviation = COMFORT_MIN - avgTemp;
        } else if (avgTemp > COMFORT_MAX) {
            deviation = avgTemp - COMFORT_MAX;
        }
        return Math.max(0.0, 100.0 - deviation * 6.0);
    }

    private String qualitative(double score) {
        if (score >= 80.0) {
            return "Clima ótimo";
        }
        if (score >= 60.0) {
            return "Clima agradável";
        }
        if (score >= 40.0) {
            return "Clima razoável";
        }
        return "Clima desafiador";
    }

    private String describeRain(double avgDailyPrecipMm) {
        if (avgDailyPrecipMm < 1.0) {
            return "tempo seco";
        }
        if (avgDailyPrecipMm < 4.0) {
            return "chuva ocasional";
        }
        return "chuva frequente";
    }
}
