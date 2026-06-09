package br.usp.lab.oo.planejador_feriado.recommendation.config;

import br.usp.lab.oo.planejador_feriado.recommendation.detector.LongWeekendDetector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecommendationConfig {

    @Bean
    public LongWeekendDetector longWeekendDetector() {
        return new LongWeekendDetector();
    }
}
