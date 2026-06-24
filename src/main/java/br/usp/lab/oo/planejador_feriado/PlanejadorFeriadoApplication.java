package br.usp.lab.oo.planejador_feriado;

import br.usp.lab.oo.planejador_feriado.common.config.CorsProperties;
import br.usp.lab.oo.planejador_feriado.common.config.ExternalApisProperties;
import br.usp.lab.oo.planejador_feriado.common.config.RateLimitProperties;
import br.usp.lab.oo.planejador_feriado.recommendation.config.ScoringProperties;
import br.usp.lab.oo.planejador_feriado.recommendation.config.TravelEstimateProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.shell.command.annotation.CommandScan;

@SpringBootApplication
@CommandScan("br.usp.lab.oo.planejador_feriado.cli")
@EnableConfigurationProperties({
        ExternalApisProperties.class,
        CorsProperties.class,
        RateLimitProperties.class,
        ScoringProperties.class,
        TravelEstimateProperties.class
})
public class PlanejadorFeriadoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanejadorFeriadoApplication.class, args);
	}

}
