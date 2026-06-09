package br.usp.lab.oo.planejador_feriado.recommendation.service;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.exchange.service.ExchangeService;
import br.usp.lab.oo.planejador_feriado.holiday.HolidayDeduplicator;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.holiday.service.HolidayService;
import br.usp.lab.oo.planejador_feriado.recommendation.detector.LongWeekendDetector;
import br.usp.lab.oo.planejador_feriado.recommendation.dto.RecommendationResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.model.LongWeekend;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import br.usp.lab.oo.planejador_feriado.recommendation.model.SkippedCandidate;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TravelRecommendation;
import br.usp.lab.oo.planejador_feriado.recommendation.strategy.ScoringStrategy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TravelRecommendationEngine {

    private static final String BRAZIL_ISO = "BR";

    private final CountryService countryService;
    private final HolidayService holidayService;
    private final ExchangeService exchangeService;
    private final List<ScoringStrategy> scoringStrategies;
    private final LongWeekendDetector longWeekendDetector;

    public TravelRecommendationEngine(
            CountryService countryService,
            HolidayService holidayService,
            ExchangeService exchangeService,
            List<ScoringStrategy> scoringStrategies,
            LongWeekendDetector longWeekendDetector) {
        this.countryService = countryService;
        this.holidayService = holidayService;
        this.exchangeService = exchangeService;
        this.scoringStrategies = scoringStrategies;
        this.longWeekendDetector = longWeekendDetector;
    }

    public RecommendationResponse recommend(RecommendationRequest request) {
        List<String> candidateCodes = resolveCandidateCodes(request);
        List<Holiday> brazilHolidays = HolidayDeduplicator.deduplicate(
                holidayService.getHolidaysInWindow(BRAZIL_ISO, request.from(), request.to())
        );
        List<LongWeekend> brazilLongWeekends = longWeekendDetector.detect(
                brazilHolidays, request.from(), request.to()
        );

        List<TravelRecommendation> recommendations = new ArrayList<>();
        List<SkippedCandidate> skipped = new ArrayList<>();

        for (String code : candidateCodes) {
            try {
                Country country = countryService.getCountryByCode(code);
                if (BRAZIL_ISO.equalsIgnoreCase(country.getIsoCode())) {
                    skipped.add(new SkippedCandidate(code, "País de origem ignorado na comparação"));
                    continue;
                }

                List<Holiday> destinationHolidays = HolidayDeduplicator.deduplicate(
                        holidayService.getHolidaysInWindow(country.getIsoCode(), request.from(), request.to())
                );
                Exchange exchange = resolveExchangeToBrl(country);

                RecommendationContext context = new RecommendationContext(
                        country,
                        destinationHolidays,
                        brazilHolidays,
                        brazilLongWeekends,
                        exchange,
                        request
                );

                recommendations.add(buildRecommendation(context));
            } catch (RuntimeException e) {
                skipped.add(new SkippedCandidate(code, e.getMessage() != null ? e.getMessage() : "Erro ao processar candidato"));
            }
        }

        recommendations.sort(Comparator.comparingDouble(TravelRecommendation::score).reversed());

        return new RecommendationResponse(
                request.from(),
                request.to(),
                brazilLongWeekends,
                recommendations,
                skipped
        );
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

    private TravelRecommendation buildRecommendation(RecommendationContext context) {
        List<ScoreEntry> breakdown = scoringStrategies.stream()
                .map(strategy -> strategy.evaluate(context))
                .toList();

        double score = breakdown.stream().mapToDouble(ScoreEntry::points).sum();
        String summary = buildSummary(context.country(), score, breakdown, context.brazilLongWeekends());

        return new TravelRecommendation(
                context.country().getIsoCode(),
                context.country().getName(),
                score,
                breakdown,
                summary
        );
    }

    private String buildSummary(
            Country country,
            double score,
            List<ScoreEntry> breakdown,
            List<LongWeekend> longWeekends) {

        List<String> highlights = new ArrayList<>();

        if (!longWeekends.isEmpty()) {
            LongWeekend best = longWeekends.stream()
                    .max(Comparator.comparingInt(LongWeekend::totalDays))
                    .orElseThrow();
            highlights.add("feriadão de " + best.totalDays() + " dias");
        }

        for (ScoreEntry entry : breakdown) {
            if ("CAMBIO".equals(entry.criterion())) {
                if (entry.points() >= 25.0) {
                    highlights.add("câmbio favorável");
                } else if (entry.points() <= 8.0 && entry.points() > 0) {
                    highlights.add("câmbio desfavorável");
                } else if (entry.points() == 0.0 && entry.justification().contains("orçamento")) {
                    highlights.add("acima do orçamento");
                } else if (entry.points() >= 35.0) {
                    highlights.add("câmbio muito favorável");
                }
            }
            if ("DIAS_LIVRES".equals(entry.criterion())) {
                highlights.add(entry.justification().toLowerCase(Locale.ROOT));
            }
        }

        String highlightText = highlights.isEmpty()
                ? "sem destaques relevantes"
                : highlights.stream().limit(3).collect(Collectors.joining(", "));

        return String.format(
                Locale.ROOT,
                "%s — score %.0f: %s",
                country.getIsoCode(),
                score,
                highlightText
        );
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
}
