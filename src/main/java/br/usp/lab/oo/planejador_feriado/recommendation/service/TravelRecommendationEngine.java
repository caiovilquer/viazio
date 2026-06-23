package br.usp.lab.oo.planejador_feriado.recommendation.service;

import br.usp.lab.oo.planejador_feriado.common.geo.GeoCalculator;
import br.usp.lab.oo.planejador_feriado.cost.model.CostOfLiving;
import br.usp.lab.oo.planejador_feriado.cost.service.CostOfLivingService;
import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.exchange.service.ExchangeService;
import br.usp.lab.oo.planejador_feriado.holiday.HolidayDeduplicator;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.holiday.service.HolidayService;
import br.usp.lab.oo.planejador_feriado.recommendation.detector.LongWeekendDetector;
import br.usp.lab.oo.planejador_feriado.recommendation.dto.RecommendationResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.filter.CandidateFilterChain;
import br.usp.lab.oo.planejador_feriado.recommendation.filter.FilterContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.LongWeekend;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoredCriterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.SkippedCandidate;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TravelRecommendation;
import br.usp.lab.oo.planejador_feriado.recommendation.strategy.ScoringStrategy;
import br.usp.lab.oo.planejador_feriado.recommendation.weight.ResolvedWeights;
import br.usp.lab.oo.planejador_feriado.recommendation.weight.WeightResolver;
import br.usp.lab.oo.planejador_feriado.weather.model.WeatherSummary;
import br.usp.lab.oo.planejador_feriado.weather.service.WeatherService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Motor de recomendação (Facade sobre os serviços de domínio + Strategy para os
 * critérios). Cada candidato é avaliado em paralelo (virtual threads): coleta-se todo
 * o dado externo (país, feriados, câmbio, clima, custo, distância), aplica-se a cadeia
 * de filtros (Chain of Responsibility) e, sobrevivendo, calcula-se o score final como
 * média ponderada das notas normalizadas (0–100) das strategies, com pesos resolvidos
 * por perfil + ajuste fino.
 */
@Service
public class TravelRecommendationEngine {

    private static final String BRAZIL_ISO = "BR";
    // Centroide aproximado do Brasil, usado quando a API de países não traz coordenadas.
    private static final double BRAZIL_FALLBACK_LAT = -10.0;
    private static final double BRAZIL_FALLBACK_LON = -55.0;

    private final CountryService countryService;
    private final HolidayService holidayService;
    private final ExchangeService exchangeService;
    private final WeatherService weatherService;
    private final CostOfLivingService costService;
    private final List<ScoringStrategy> scoringStrategies;
    private final LongWeekendDetector longWeekendDetector;
    private final WeightResolver weightResolver;
    private final CandidateFilterChain filterChain;

    public TravelRecommendationEngine(
            CountryService countryService,
            HolidayService holidayService,
            ExchangeService exchangeService,
            WeatherService weatherService,
            CostOfLivingService costService,
            List<ScoringStrategy> scoringStrategies,
            LongWeekendDetector longWeekendDetector,
            WeightResolver weightResolver,
            CandidateFilterChain filterChain) {
        this.countryService = countryService;
        this.holidayService = holidayService;
        this.exchangeService = exchangeService;
        this.weatherService = weatherService;
        this.costService = costService;
        this.scoringStrategies = scoringStrategies;
        this.longWeekendDetector = longWeekendDetector;
        this.weightResolver = weightResolver;
        this.filterChain = filterChain;
    }

    public RecommendationResponse recommend(RecommendationRequest request) {
        ResolvedWeights weights = weightResolver.resolve(request.profile(), request.weightOverrides());

        List<Holiday> brazilHolidays = HolidayDeduplicator.deduplicate(
                holidayService.getHolidaysInWindow(BRAZIL_ISO, request.from(), request.to())
        );
        List<LongWeekend> brazilLongWeekends = longWeekendDetector.detect(
                brazilHolidays, request.from(), request.to()
        );
        BrazilReference brazil = loadBrazilReference(brazilHolidays, brazilLongWeekends);

        List<String> candidateCodes = resolveCandidateCodes(request);

        List<TravelRecommendation> recommendations = new ArrayList<>();
        List<SkippedCandidate> skipped = new ArrayList<>();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<CandidateResult>> futures = candidateCodes.stream()
                    .map(code -> executor.submit(() -> evaluateCandidate(code, request, brazil, weights)))
                    .toList();

            for (Future<CandidateResult> future : futures) {
                CandidateResult result = future.get();
                if (result.recommendation() != null) {
                    recommendations.add(result.recommendation());
                } else {
                    skipped.add(result.skipped());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrompido ao gerar recomendações", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Erro ao gerar recomendações", e.getCause());
        }

        recommendations.sort(Comparator.comparingDouble(TravelRecommendation::score).reversed());
        List<TravelRecommendation> limited = recommendations.stream()
                .limit(Math.max(request.limit(), 0))
                .toList();

        return new RecommendationResponse(
                request.from(),
                request.to(),
                weights.profileName(),
                weights.asKeyedMap(),
                brazilLongWeekends,
                limited,
                skipped
        );
    }

    private BrazilReference loadBrazilReference(List<Holiday> holidays, List<LongWeekend> longWeekends) {
        double lat = BRAZIL_FALLBACK_LAT;
        double lon = BRAZIL_FALLBACK_LON;
        CostOfLiving cost = null;
        try {
            Country brazil = countryService.getCountryByCode(BRAZIL_ISO);
            if (brazil.hasCoordinates()) {
                lat = brazil.getLatitude();
                lon = brazil.getLongitude();
            }
        } catch (RuntimeException ignored) {
            // mantém o fallback de coordenadas
        }
        try {
            cost = costService.getPriceLevel(BRAZIL_ISO).orElse(null);
        } catch (RuntimeException ignored) {
            // custo comparativo fica indisponível
        }
        return new BrazilReference(lat, lon, cost, holidays, longWeekends);
    }

    private CandidateResult evaluateCandidate(
            String code,
            RecommendationRequest request,
            BrazilReference brazil,
            ResolvedWeights weights) {

        try {
            Country country = countryService.getCountryByCode(code);
            if (BRAZIL_ISO.equalsIgnoreCase(country.getIsoCode())) {
                return CandidateResult.skipped(new SkippedCandidate(code, "País de origem ignorado na comparação"));
            }

            Exchange exchange = resolveExchangeToBrl(country);

            Optional<String> rejection = filterChain.reject(new FilterContext(code, country, exchange, request));
            if (rejection.isPresent()) {
                return CandidateResult.skipped(new SkippedCandidate(code, rejection.get()));
            }

            List<Holiday> destinationHolidays = HolidayDeduplicator.deduplicate(
                    holidayService.getHolidaysInWindow(country.getIsoCode(), request.from(), request.to())
            );

            WeatherSummary weather = resolveWeather(country, request);
            CostOfLiving destinationCost = resolveCost(country.getIsoCode());
            Double distanceKm = resolveDistance(brazil, country);

            RecommendationContext context = new RecommendationContext(
                    country,
                    destinationHolidays,
                    brazil.holidays(),
                    brazil.longWeekends(),
                    exchange,
                    weather,
                    destinationCost,
                    brazil.cost(),
                    distanceKm,
                    request
            );

            return CandidateResult.recommendation(buildRecommendation(context, weights));
        } catch (RuntimeException e) {
            return CandidateResult.skipped(
                    new SkippedCandidate(code, e.getMessage() != null ? e.getMessage() : "Erro ao processar candidato"));
        }
    }

    private TravelRecommendation buildRecommendation(RecommendationContext context, ResolvedWeights weights) {
        List<ScoreEntry> entries = scoringStrategies.stream()
                .map(strategy -> strategy.evaluate(context))
                .toList();

        double availableWeightSum = entries.stream()
                .filter(ScoreEntry::available)
                .mapToDouble(entry -> weights.weightOf(entry.criterion()))
                .sum();

        List<ScoredCriterion> breakdown = new ArrayList<>();
        double finalScore = 0.0;
        for (ScoreEntry entry : entries) {
            double weight = weights.weightOf(entry.criterion());
            double contribution = (entry.available() && availableWeightSum > 0)
                    ? (weight / availableWeightSum) * entry.score()
                    : 0.0;
            finalScore += contribution;
            breakdown.add(new ScoredCriterion(
                    entry.criterion().key(),
                    entry.criterion().label(),
                    entry.criterion().icon(),
                    entry.available(),
                    entry.score(),
                    weight,
                    contribution,
                    entry.justification()
            ));
        }

        breakdown.sort(Comparator.comparingDouble(ScoredCriterion::contribution).reversed());
        List<String> highlights = buildHighlights(breakdown);
        String summary = buildSummary(context.country().getIsoCode(), finalScore, highlights);

        return new TravelRecommendation(
                context.country().getIsoCode(),
                context.country().getName(),
                Math.round(finalScore * 10.0) / 10.0,
                breakdown,
                highlights,
                summary
        );
    }

    private List<String> buildHighlights(List<ScoredCriterion> breakdown) {
        List<String> highlights = new ArrayList<>();
        for (ScoredCriterion criterion : breakdown) {
            if (highlights.size() >= 3) {
                break;
            }
            if (!criterion.available() || criterion.score() < 65.0) {
                continue;
            }
            highlightFor(criterion).ifPresent(highlights::add);
        }
        return highlights;
    }

    private Optional<String> highlightFor(ScoredCriterion criterion) {
        return Criterion.fromKey(criterion.criterion()).map(c -> switch (c) {
            case HOLIDAYS -> "ótimo para feriadão";
            case WEATHER -> criterion.score() >= 80 ? "clima ótimo" : "clima agradável";
            case COST -> "custo de vida baixo";
            case EXCHANGE -> "câmbio favorável";
            case DISTANCE -> "pertinho do Brasil";
            case FESTIVITIES -> "festividades no destino";
        });
    }

    private String buildSummary(String code, double score, List<String> highlights) {
        String highlightText = highlights.isEmpty()
                ? "sem grandes destaques"
                : String.join(", ", highlights);
        return String.format(Locale.ROOT, "%s — score %.0f: %s", code, score, highlightText);
    }

    private WeatherSummary resolveWeather(Country country, RecommendationRequest request) {
        if (!country.hasCoordinates()) {
            return null;
        }
        try {
            return weatherService.getClimateForWindow(
                    country.getLatitude(), country.getLongitude(), request.from(), request.to()).orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private CostOfLiving resolveCost(String isoCode) {
        try {
            return costService.getPriceLevel(isoCode).orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Double resolveDistance(BrazilReference brazil, Country country) {
        if (!country.hasCoordinates()) {
            return null;
        }
        return GeoCalculator.haversineKm(
                brazil.lat(), brazil.lon(), country.getLatitude(), country.getLongitude());
    }

    private Exchange resolveExchangeToBrl(Country country) {
        String currency = country.getMainCurrency();
        if (currency == null || currency.isBlank() || currency.equalsIgnoreCase("BRL")) {
            return null;
        }
        try {
            return exchangeService.getExchangeRate(currency);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private List<String> resolveCandidateCodes(RecommendationRequest request) {
        Set<String> codes = new LinkedHashSet<>();

        if (request.countryCodes() != null && !request.countryCodes().isEmpty()) {
            for (String code : request.countryCodes()) {
                if (code != null && !code.isBlank()) {
                    codes.add(code.trim().toUpperCase(Locale.ROOT));
                }
            }
            return List.copyOf(codes);
        }

        List<Country> countries = countryService.getCountriesByRegion(request.region(), request.limit());
        for (Country country : countries) {
            codes.add(country.getIsoCode().toUpperCase(Locale.ROOT));
        }
        return List.copyOf(codes);
    }

    private record BrazilReference(
            double lat,
            double lon,
            CostOfLiving cost,
            List<Holiday> holidays,
            List<LongWeekend> longWeekends) {
    }

    private record CandidateResult(TravelRecommendation recommendation, SkippedCandidate skipped) {
        static CandidateResult recommendation(TravelRecommendation recommendation) {
            return new CandidateResult(recommendation, null);
        }

        static CandidateResult skipped(SkippedCandidate skipped) {
            return new CandidateResult(null, skipped);
        }
    }
}
