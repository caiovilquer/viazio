package br.usp.lab.oo.planejador_feriado.enrichment.service;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.demographics.model.Demographics;
import br.usp.lab.oo.planejador_feriado.demographics.service.DemographicsService;
import br.usp.lab.oo.planejador_feriado.enrichment.model.DestinationProfile;
import br.usp.lab.oo.planejador_feriado.enrichment.model.WikiSummary;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Facade (GoF) que reúne os dados puramente descritivos de um destino — população
 * (Banco Mundial) e resumo/imagem da Wikipédia — sem qualquer papel no score. Cada
 * sub-busca degrada graciosamente: se uma falhar, as demais seguem disponíveis, no
 * mesmo espírito das strategies de recomendação.
 */
@Service
public class DestinationProfileService {

    private final DemographicsService demographicsService;
    private final WikipediaService wikipediaService;

    public DestinationProfileService(DemographicsService demographicsService, WikipediaService wikipediaService) {
        this.demographicsService = demographicsService;
        this.wikipediaService = wikipediaService;
    }

    public DestinationProfile buildProfile(Country country) {
        Optional<Demographics> population = resolvePopulation(country);
        Optional<WikiSummary> wiki = resolveWikiSummary(country);

        return new DestinationProfile(
                country.getFlagEmoji(),
                population.map(Demographics::population).orElse(null),
                population.map(Demographics::year).orElse(null),
                wiki.map(WikiSummary::description).orElse(null),
                wiki.map(WikiSummary::extract).orElse(null),
                wiki.map(WikiSummary::imageUrl).orElse(null),
                wiki.map(WikiSummary::pageUrl).orElse(null)
        );
    }

    private Optional<Demographics> resolvePopulation(Country country) {
        try {
            return demographicsService.getPopulation(country.getIsoCode());
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private Optional<WikiSummary> resolveWikiSummary(Country country) {
        try {
            return wikipediaService.getCountrySummary(country.getLocalizedName(), country.getName());
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
