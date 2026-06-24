package br.usp.lab.oo.planejador_feriado.recommendation.service;

import br.usp.lab.oo.planejador_feriado.common.exception.ResourceNotFoundException;
import br.usp.lab.oo.planejador_feriado.common.geo.GeoCalculator;
import br.usp.lab.oo.planejador_feriado.cost.model.CostOfLiving;
import br.usp.lab.oo.planejador_feriado.cost.service.CostOfLivingService;
import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import br.usp.lab.oo.planejador_feriado.enrichment.model.DestinationProfile;
import br.usp.lab.oo.planejador_feriado.enrichment.service.DestinationProfileService;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.exchange.service.ExchangeService;
import br.usp.lab.oo.planejador_feriado.holiday.HolidayDeduplicator;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.holiday.service.HolidayService;
import br.usp.lab.oo.planejador_feriado.recommendation.dto.RecommendationResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.filter.CandidateFilterChain;
import br.usp.lab.oo.planejador_feriado.recommendation.filter.FilterContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.DataQuality;
import br.usp.lab.oo.planejador_feriado.recommendation.model.OriginReference;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoredCriterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.SkippedCandidate;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TravelRecommendation;
import br.usp.lab.oo.planejador_feriado.recommendation.model.WindowAssessment;
import br.usp.lab.oo.planejador_feriado.recommendation.strategy.ScoringStrategy;
import br.usp.lab.oo.planejador_feriado.recommendation.weight.ResolvedWeights;
import br.usp.lab.oo.planejador_feriado.recommendation.weight.WeightResolver;
import br.usp.lab.oo.planejador_feriado.weather.model.WeatherSummary;
import br.usp.lab.oo.planejador_feriado.weather.service.WeatherService;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
 * Compara todos os candidatos elegíveis, separa qualidade da janela e do destino,
 * e reduz a nota final quando a cobertura de dados é baixa. Dados descritivos caros
 * são carregados somente para os resultados que sobreviveram ao ranking.
 */
@Service
public class TravelRecommendationEngine {

    private static final int MAX_EXPLICIT_CANDIDATES = 50;
    private static final int MAX_REGION_CANDIDATES = 60;
    private static final double DESTINATION_SHARE = 0.80;
    private static final double WINDOW_SHARE = 0.20;
    private static final double MIN_CONFIDENCE_MULTIPLIER = 0.75;

    private final CountryService countryService;
    private final HolidayService holidayService;
    private final ExchangeService exchangeService;
    private final WeatherService weatherService;
    private final CostOfLivingService costService;
    private final DestinationProfileService profileService;
    private final List<ScoringStrategy> scoringStrategies;
    private final TravelWindowEvaluator windowEvaluator;
    private final WeightResolver weightResolver;
    private final CandidateFilterChain filterChain;

    public TravelRecommendationEngine(
            CountryService countryService,
            HolidayService holidayService,
            ExchangeService exchangeService,
            WeatherService weatherService,
            CostOfLivingService costService,
            DestinationProfileService profileService,
            List<ScoringStrategy> scoringStrategies,
            TravelWindowEvaluator windowEvaluator,
            WeightResolver weightResolver,
            CandidateFilterChain filterChain) {
        this.countryService = countryService;
        this.holidayService = holidayService;
        this.exchangeService = exchangeService;
        this.weatherService = weatherService;
        this.costService = costService;
        this.profileService = profileService;
        this.scoringStrategies = scoringStrategies;
        this.windowEvaluator = windowEvaluator;
        this.weightResolver = weightResolver;
        this.filterChain = filterChain;
    }

    public RecommendationResponse recommend(RecommendationRequest request) {
        ResolvedWeights weights = weightResolver.resolve(request.profile(), request.weightOverrides());
        OriginData origin = loadOrigin(request);
        WindowAssessment window = windowEvaluator.evaluate(origin.holidays(), request.from(), request.to());
        List<String> candidateCodes = resolveCandidateCodes(request);

        List<TravelRecommendation> recommendations = new ArrayList<>();
        List<SkippedCandidate> skipped = new ArrayList<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<CandidateResult>> futures = candidateCodes.stream()
                    .map(code -> executor.submit(() -> evaluateCandidate(code, request, origin, window, weights)))
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

        recommendations.sort(Comparator.comparingDouble(TravelRecommendation::tripScore).reversed()
                .thenComparing(Comparator.comparingDouble(
                        (TravelRecommendation recommendation) ->
                                recommendation.dataQuality().confidenceScore()).reversed())
                .thenComparing(TravelRecommendation::countryCode));
        List<TravelRecommendation> limited = recommendations.stream()
                .limit(request.limit())
                .map(this::enrichFinalist)
                .toList();

        return new RecommendationResponse(
                request.from(),
                request.to(),
                Instant.now(),
                origin.reference(),
                weights.profileName(),
                weights.asKeyedMap(),
                window,
                limited,
                skipped);
    }

    private OriginData loadOrigin(RecommendationRequest request) {
        Country country = countryService.getCountryByCode(request.originCountryCode());
        double latitude = request.originLatitude() != null
                ? request.originLatitude()
                : requireCoordinate(country.getLatitude(), "latitude");
        double longitude = request.originLongitude() != null
                ? request.originLongitude()
                : requireCoordinate(country.getLongitude(), "longitude");
        if ((request.originLatitude() == null) != (request.originLongitude() == null)) {
            throw new IllegalArgumentException("Latitude e longitude da origem devem ser informadas juntas");
        }

        List<Holiday> holidays = HolidayDeduplicator.deduplicate(
                holidayService.getHolidaysInWindow(
                        country.getIsoCode(),
                        request.originSubdivisionCode(),
                        request.from(),
                        request.to()));
        CostOfLiving cost = resolveCost(country.getIsoCode());
        return new OriginData(
                new OriginReference(
                        country.getIsoCode(),
                        request.originSubdivisionCode(),
                        latitude,
                        longitude),
                cost,
                holidays);
    }

    private double requireCoordinate(Double value, String coordinate) {
        if (value == null) {
            throw new IllegalArgumentException("Coordenada de " + coordinate + " indisponível para a origem");
        }
        return value;
    }

    private CandidateResult evaluateCandidate(
            String code,
            RecommendationRequest request,
            OriginData origin,
            WindowAssessment window,
            ResolvedWeights weights) {
        try {
            Country country = countryService.getCountryByCode(code);
            if (origin.reference().countryCode().equalsIgnoreCase(country.getIsoCode())) {
                return CandidateResult.skipped(new SkippedCandidate(code, "País de origem ignorado na comparação"));
            }

            Optional<String> rejection = filterChain.reject(new FilterContext(code, country, request));
            if (rejection.isPresent()) {
                return CandidateResult.skipped(new SkippedCandidate(code, rejection.get()));
            }

            List<Holiday> destinationHolidays = HolidayDeduplicator.deduplicate(
                    holidayService.getHolidaysInWindow(country.getIsoCode(), request.from(), request.to()));
            Exchange exchange = resolveExchangeToBrl(country);
            WeatherSummary weather = resolveWeather(country, request);
            CostOfLiving destinationCost = resolveCost(country.getIsoCode());
            Double distanceKm = resolveDistance(origin.reference(), country);

            RecommendationContext context = new RecommendationContext(
                    country,
                    destinationHolidays,
                    exchange,
                    weather,
                    destinationCost,
                    origin.cost(),
                    distanceKm,
                    null,
                    request);
            return CandidateResult.recommendation(buildRecommendation(context, window, weights));
        } catch (ResourceNotFoundException e) {
            return CandidateResult.skipped(new SkippedCandidate(code, e.getMessage()));
        } catch (RuntimeException e) {
            return CandidateResult.skipped(new SkippedCandidate(code, "Dados essenciais indisponíveis"));
        }
    }

    private TravelRecommendation buildRecommendation(
            RecommendationContext context,
            WindowAssessment window,
            ResolvedWeights weights) {
        List<ScoreEntry> entries = scoringStrategies.stream()
                .map(strategy -> strategy.evaluate(context))
                .toList();
        double availableWeight = entries.stream()
                .filter(ScoreEntry::available)
                .mapToDouble(entry -> weights.weightOf(entry.criterion()))
                .sum();

        List<ScoredCriterion> breakdown = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        double destinationScore = 0.0;
        for (ScoreEntry entry : entries) {
            double effectiveWeight = entry.available() && availableWeight > 0.0
                    ? weights.weightOf(entry.criterion()) / availableWeight
                    : 0.0;
            double contribution = effectiveWeight * entry.score();
            destinationScore += contribution;
            if (!entry.available()) {
                missing.add(entry.criterion().key());
            }
            breakdown.add(new ScoredCriterion(
                    entry.criterion().key(),
                    entry.criterion().label(),
                    entry.criterion().icon(),
                    entry.available(),
                    round(entry.score()),
                    round(effectiveWeight),
                    round(contribution),
                    entry.justification()));
        }
        breakdown.sort(Comparator.comparingDouble(ScoredCriterion::contribution).reversed());

        double coverage = Math.max(0.0, Math.min(1.0, availableWeight));
        double confidence = coverage * 100.0;
        double combined = DESTINATION_SHARE * destinationScore + WINDOW_SHARE * window.score();
        double confidenceMultiplier = MIN_CONFIDENCE_MULTIPLIER
                + (1.0 - MIN_CONFIDENCE_MULTIPLIER) * coverage;
        double tripScore = combined * confidenceMultiplier;
        DataQuality quality = new DataQuality(
                round(coverage),
                round(confidence),
                entries.size() - missing.size(),
                entries.size(),
                List.copyOf(missing));

        List<String> highlights = buildHighlights(breakdown);
        List<String> tradeoffs = buildTradeoffs(breakdown);
        String summary = buildSummary(context.country().getDisplayName(), tripScore, highlights, quality);
        return new TravelRecommendation(
                context.country().getIsoCode(),
                context.country().getDisplayName(),
                round(destinationScore),
                window.score(),
                round(tripScore),
                quality,
                breakdown,
                highlights,
                tradeoffs,
                summary,
                context.exchangeToBrl(),
                null);
    }

    private List<String> buildHighlights(List<ScoredCriterion> breakdown) {
        return breakdown.stream()
                .filter(ScoredCriterion::available)
                .filter(criterion -> criterion.score() >= 65.0)
                .limit(3)
                .map(this::highlightFor)
                .toList();
    }

    private String highlightFor(ScoredCriterion criterion) {
        return Criterion.fromKey(criterion.criterion()).map(value -> switch (value) {
            case WEATHER -> criterion.score() >= 80.0 ? "clima ótimo" : "clima agradável";
            case COST -> "bom poder de compra";
            case DISTANCE -> "deslocamento geográfico menor";
            case FESTIVITIES -> "calendário local interessante";
        }).orElse(criterion.label());
    }

    private List<String> buildTradeoffs(List<ScoredCriterion> breakdown) {
        return breakdown.stream()
                .filter(criterion -> !criterion.available() || criterion.score() < 45.0)
                .limit(3)
                .map(criterion -> criterion.available()
                        ? criterion.justification()
                        : criterion.label() + ": dado indisponível")
                .toList();
    }

    private String buildSummary(
            String countryName,
            double score,
            List<String> highlights,
            DataQuality quality) {
        String reasons = highlights.isEmpty() ? "sem destaque dominante" : String.join(", ", highlights);
        return String.format(Locale.ROOT,
                "%s — nota de viagem %.0f: %s; confiança %.0f%%",
                countryName,
                score,
                reasons,
                quality.confidenceScore());
    }

    private TravelRecommendation enrichFinalist(TravelRecommendation recommendation) {
        try {
            Country country = countryService.getCountryByCode(recommendation.countryCode());
            DestinationProfile profile = profileService.buildProfile(country);
            return new TravelRecommendation(
                    recommendation.countryCode(),
                    recommendation.countryName(),
                    recommendation.destinationScore(),
                    recommendation.windowScore(),
                    recommendation.tripScore(),
                    recommendation.dataQuality(),
                    recommendation.breakdown(),
                    recommendation.highlights(),
                    recommendation.tradeoffs(),
                    recommendation.summary(),
                    recommendation.exchangeToBrl(),
                    profile);
        } catch (RuntimeException ignored) {
            return recommendation;
        }
    }

    private WeatherSummary resolveWeather(Country country, RecommendationRequest request) {
        if (!country.hasCoordinates()) {
            return null;
        }
        try {
            return weatherService.getClimateForWindow(
                    country.getLatitude(),
                    country.getLongitude(),
                    request.from(),
                    request.to()).orElse(null);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private CostOfLiving resolveCost(String isoCode) {
        try {
            return costService.getPriceLevel(isoCode).orElse(null);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private Double resolveDistance(OriginReference origin, Country country) {
        if (!country.hasCoordinates()) {
            return null;
        }
        return GeoCalculator.haversineKm(
                origin.latitude(),
                origin.longitude(),
                country.getLatitude(),
                country.getLongitude());
    }

    private Exchange resolveExchangeToBrl(Country country) {
        String currency = country.getMainCurrency();
        if (currency == null || currency.isBlank() || currency.equalsIgnoreCase("BRL")) {
            return null;
        }
        try {
            return exchangeService.getExchangeRate(currency);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private List<String> resolveCandidateCodes(RecommendationRequest request) {
        Set<String> codes = new LinkedHashSet<>();
        if (!request.countryCodes().isEmpty()) {
            if (request.countryCodes().size() > MAX_EXPLICIT_CANDIDATES) {
                throw new IllegalArgumentException(
                        "No máximo " + MAX_EXPLICIT_CANDIDATES + " países podem ser comparados por requisição");
            }
            request.countryCodes().stream()
                    .filter(code -> code != null && !code.isBlank())
                    .map(code -> code.trim().toUpperCase(Locale.ROOT))
                    .forEach(codes::add);
            return List.copyOf(codes);
        }

        List<Country> countries = countryService.getCountriesByRegion(request.region());
        if (countries.size() > MAX_REGION_CANDIDATES) {
            throw new IllegalArgumentException("Região excede o limite operacional de candidatos");
        }
        countries.stream()
                .map(Country::getIsoCode)
                .map(code -> code.toUpperCase(Locale.ROOT))
                .forEach(codes::add);
        return List.copyOf(codes);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record OriginData(OriginReference reference, CostOfLiving cost, List<Holiday> holidays) {
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
