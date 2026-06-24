package br.usp.lab.oo.planejador_feriado.meta.service;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import br.usp.lab.oo.planejador_feriado.destination.model.DestinationCity;
import br.usp.lab.oo.planejador_feriado.destination.service.DestinationCatalogService;
import br.usp.lab.oo.planejador_feriado.meta.dto.ApiLimits;
import br.usp.lab.oo.planejador_feriado.meta.dto.ApiMetaResponse;
import br.usp.lab.oo.planejador_feriado.meta.dto.CityOption;
import br.usp.lab.oo.planejador_feriado.meta.dto.CountryOption;
import br.usp.lab.oo.planejador_feriado.meta.dto.CriterionOption;
import br.usp.lab.oo.planejador_feriado.meta.dto.DataSourceInfo;
import br.usp.lab.oo.planejador_feriado.meta.dto.ProfileOption;
import br.usp.lab.oo.planejador_feriado.meta.dto.RegionOption;
import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import br.usp.lab.oo.planejador_feriado.recommendation.config.RecommendationLimits;
import br.usp.lab.oo.planejador_feriado.recommendation.weight.ResolvedWeights;
import br.usp.lab.oo.planejador_feriado.recommendation.weight.WeightResolver;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ApiMetaService {

    private final CountryService countryService;
    private final DestinationCatalogService destinationCatalogService;
    private final WeightResolver weightResolver;

    private volatile ApiMetaResponse cached;

    public ApiMetaService(
            CountryService countryService,
            DestinationCatalogService destinationCatalogService,
            WeightResolver weightResolver) {
        this.countryService = countryService;
        this.destinationCatalogService = destinationCatalogService;
        this.weightResolver = weightResolver;
    }

    public ApiMetaResponse getMetadata() {
        ApiMetaResponse current = cached;
        if (current == null) {
            synchronized (this) {
                current = cached;
                if (current == null) {
                    current = build();
                    cached = current;
                }
            }
        }
        return current;
    }

    private ApiMetaResponse build() {
        ResolvedWeights defaults = weightResolver.resolve(null, Map.of());
        List<CriterionOption> criteria = List.of(Criterion.values()).stream()
                .map(criterion -> new CriterionOption(
                        criterion.key(),
                        criterion.label(),
                        criterion.icon(),
                        defaults.weightOf(criterion)))
                .toList();

        List<ProfileOption> profiles = weightResolver.availableProfiles().stream()
                .map(profile -> new ProfileOption(
                        profile,
                        profileLabel(profile),
                        weightResolver.resolve(profile, Map.of()).asKeyedMap()))
                .toList();

        List<CountryOption> countries = countryService.getAllTravelEligibleCountries().stream()
                .sorted(Comparator.comparing(Country::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toCountryOption)
                .toList();

        return new ApiMetaResponse(
                "v1",
                "2026-06-23",
                new ApiLimits(
                        RecommendationLimits.MAX_RECOMMENDATION_WINDOW_DAYS,
                        RecommendationLimits.MAX_BEST_WINDOWS_PERIOD_DAYS,
                        RecommendationLimits.MAX_RESULTS,
                        RecommendationLimits.MAX_EXPLICIT_CANDIDATES,
                        RecommendationLimits.MAX_REGION_CANDIDATES,
                        RecommendationLimits.MAX_TRAVELERS),
                List.of(
                        new RegionOption("Africa", "África"),
                        new RegionOption("Americas", "Américas"),
                        new RegionOption("Asia", "Ásia"),
                        new RegionOption("Europe", "Europa"),
                        new RegionOption("Oceania", "Oceania")),
                criteria,
                profiles,
                countries,
                List.of(
                        new DataSourceInfo("countries", "mledoze/countries", "STATIC",
                                "países, moedas e regiões"),
                        new DataSourceInfo("destinations", "Wikidata", "STATIC",
                                "capitais, coordenadas e offsets UTC"),
                        new DataSourceInfo("holidays", "Nager.Date", "LIVE",
                                "feriados nacionais e por subdivisão"),
                        new DataSourceInfo("weather", "Open-Meteo", "LIVE_AND_HISTORICAL",
                                "previsão e climatologia"),
                        new DataSourceInfo("economy", "World Bank", "LIVE_CACHED",
                                "PPP, câmbio oficial e população"),
                        new DataSourceInfo("exchange", "AwesomeAPI", "LIVE_CACHED",
                                "câmbio nominal informativo"),
                        new DataSourceInfo("content", "Wikipedia", "LIVE_CACHED",
                                "resumo, imagem e link do destino")),
                Map.of(
                        "bestWindows", true,
                        "customWeights", true,
                        "groundBudgetFilter", true,
                        "customOriginCoordinates", true,
                        "liveCommercialPrices", false));
    }

    private CountryOption toCountryOption(Country country) {
        List<DestinationCity> catalogCities = destinationCatalogService.getCities(country.getIsoCode());
        List<CityOption> cities = catalogCities.stream()
                .map(city -> new CityOption(
                        city.name(),
                        city.latitude(),
                        city.longitude(),
                        city.primary()))
                .toList();
        String defaultCity = cities.stream()
                .filter(CityOption::primary)
                .map(CityOption::name)
                .findFirst()
                .orElseGet(() -> country.getCapitals().isEmpty() ? null : country.getCapitals().get(0));
        return new CountryOption(
                country.getIsoCode(),
                country.getDisplayName(),
                country.getFlagEmoji(),
                country.getRegion(),
                country.getSubregion(),
                defaultCity,
                cities);
    }

    private String profileLabel(String profile) {
        return switch (profile.toLowerCase(Locale.ROOT)) {
            case "economico" -> "Econômico";
            case "clima-perfeito" -> "Clima perfeito";
            case "aventura" -> "Aventura";
            case "cultural" -> "Cultural";
            case "equilibrado" -> "Equilibrado";
            default -> profile;
        };
    }
}
