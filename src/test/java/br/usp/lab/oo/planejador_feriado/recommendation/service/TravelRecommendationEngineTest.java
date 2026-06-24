package br.usp.lab.oo.planejador_feriado.recommendation.service;

import br.usp.lab.oo.planejador_feriado.cost.model.CostOfLiving;
import br.usp.lab.oo.planejador_feriado.cost.service.CostOfLivingService;
import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import br.usp.lab.oo.planejador_feriado.enrichment.service.DestinationProfileService;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.exchange.service.ExchangeService;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.holiday.service.HolidayService;
import br.usp.lab.oo.planejador_feriado.recommendation.config.ScoringProperties;
import br.usp.lab.oo.planejador_feriado.recommendation.detector.LongWeekendDetector;
import br.usp.lab.oo.planejador_feriado.recommendation.dto.RecommendationResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.filter.CandidateFilterChain;
import br.usp.lab.oo.planejador_feriado.recommendation.filter.ExcludedCountriesFilter;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TravelRecommendation;
import br.usp.lab.oo.planejador_feriado.recommendation.strategy.CostOfLivingStrategy;
import br.usp.lab.oo.planejador_feriado.recommendation.strategy.DestinationFestivitiesStrategy;
import br.usp.lab.oo.planejador_feriado.recommendation.strategy.DistanceStrategy;
import br.usp.lab.oo.planejador_feriado.recommendation.strategy.WeatherStrategy;
import br.usp.lab.oo.planejador_feriado.recommendation.weight.WeightResolver;
import br.usp.lab.oo.planejador_feriado.weather.model.WeatherSummary;
import br.usp.lab.oo.planejador_feriado.weather.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TravelRecommendationEngineTest {

    private static final LocalDate FROM = LocalDate.of(2026, 9, 4);
    private static final LocalDate TO = LocalDate.of(2026, 9, 7);

    @Mock
    private CountryService countryService;
    @Mock
    private HolidayService holidayService;
    @Mock
    private ExchangeService exchangeService;
    @Mock
    private WeatherService weatherService;
    @Mock
    private CostOfLivingService costService;
    @Mock
    private DestinationProfileService profileService;

    private TravelRecommendationEngine engine;

    @BeforeEach
    void setUp() {
        WeightResolver weightResolver = new WeightResolver(new ScoringProperties(
                Map.of("weather", 0.30, "cost", 0.30, "distance", 0.25, "festivities", 0.15),
                Map.of()));
        LongWeekendDetector detector = new LongWeekendDetector();
        engine = new TravelRecommendationEngine(
                countryService,
                holidayService,
                exchangeService,
                weatherService,
                costService,
                profileService,
                List.of(
                        new WeatherStrategy(),
                        new CostOfLivingStrategy(),
                        new DistanceStrategy(),
                        new DestinationFestivitiesStrategy()),
                new TravelWindowEvaluator(detector),
                weightResolver,
                new CandidateFilterChain(List.of(new ExcludedCountriesFilter())));

        Country brazil = country("Brazil", "BR", "BRL", -10.0, -55.0);
        when(countryService.getCountryByCode("BR")).thenReturn(brazil);
        when(holidayService.getHolidaysInWindow(eq("BR"), eq(null), eq(FROM), eq(TO)))
                .thenReturn(List.of(new Holiday(
                        LocalDate.of(2026, 9, 7),
                        "Independence Day",
                        "Independência do Brasil",
                        List.of("Public"))));
        when(costService.getPriceLevel(anyString())).thenReturn(Optional.empty());
        when(weatherService.getClimateForWindow(
                org.mockito.ArgumentMatchers.anyDouble(),
                org.mockito.ArgumentMatchers.anyDouble(),
                eq(FROM),
                eq(TO))).thenReturn(Optional.empty());
    }

    @Test
    void nominalExchangeRateDoesNotDetermineRanking() {
        Country japan = country("Japan", "JP", "JPY", 36.0, 138.0);
        Country argentina = country("Argentina", "AR", "ARS", -34.0, -64.0);
        stubCandidate(japan);
        stubCandidate(argentina);
        when(exchangeService.getExchangeRate("JPY")).thenReturn(new Exchange("JPY", 0.035));
        when(exchangeService.getExchangeRate("ARS")).thenReturn(new Exchange("ARS", 0.006));

        RecommendationResponse response = engine.recommend(
                new RecommendationRequest(FROM, TO, List.of("JP", "AR"), null, 10));

        assertEquals("AR", response.recommendations().get(0).countryCode());
        assertTrue(response.recommendations().get(0).destinationScore()
                > response.recommendations().get(1).destinationScore());
    }

    @Test
    void evaluatesWholeRegionBeforeApplyingResultLimit() {
        Country far = country("Far", "AA", "AAA", 40.0, 140.0);
        Country medium = country("Medium", "BB", "BBB", 20.0, 20.0);
        Country near = country("Near", "CC", "CCC", -20.0, -60.0);
        when(countryService.getCountriesByRegion("Test")).thenReturn(List.of(far, medium, near));
        stubCandidate(far);
        stubCandidate(medium);
        stubCandidate(near);

        RecommendationResponse response = engine.recommend(
                new RecommendationRequest(FROM, TO, List.of(), "Test", 1));

        assertEquals(1, response.recommendations().size());
        assertEquals("CC", response.recommendations().get(0).countryCode());
        verify(countryService).getCountryByCode("AA");
        verify(countryService).getCountryByCode("BB");
        verify(countryService, org.mockito.Mockito.atLeastOnce()).getCountryByCode("CC");
    }

    @Test
    void exposesAndPenalizesIncompleteDataCoverage() {
        Country complete = country("Complete", "CP", "CPC", -20.0, -60.0);
        Country sparse = country("Sparse", "SP", "SPC", -20.0, -60.0);
        stubCandidate(complete);
        stubCandidate(sparse);
        when(weatherService.getClimateForWindow(eq(-20.0), eq(-60.0), eq(FROM), eq(TO)))
                .thenReturn(Optional.of(new WeatherSummary(24.0, 0.5, 0.1, 1.5,
                        40, 10,
                        br.usp.lab.oo.planejador_feriado.weather.model.WeatherSourceType.CLIMATOLOGY,
                        FROM.minusYears(10),
                        TO.minusYears(1))));
        when(costService.getPriceLevel("BR")).thenReturn(Optional.of(new CostOfLiving("BR", 0.6, "2024")));
        when(costService.getPriceLevel("CP")).thenReturn(Optional.of(new CostOfLiving("CP", 0.45, "2024")));
        when(costService.getPriceLevel("SP")).thenReturn(Optional.empty());

        RecommendationResponse response = engine.recommend(
                new RecommendationRequest(FROM, TO, List.of("CP", "SP"), null, 10));

        TravelRecommendation completeResult = response.recommendations().stream()
                .filter(item -> item.countryCode().equals("CP"))
                .findFirst()
                .orElseThrow();
        TravelRecommendation sparseResult = response.recommendations().stream()
                .filter(item -> item.countryCode().equals("SP"))
                .findFirst()
                .orElseThrow();
        assertTrue(completeResult.dataQuality().confidenceScore()
                > sparseResult.dataQuality().confidenceScore());
        assertTrue(sparseResult.dataQuality().missingCriteria().contains("cost"));
    }

    @Test
    void skipsInvalidCandidateWithoutFailingRanking() {
        Country japan = country("Japan", "JP", "JPY", 36.0, 138.0);
        stubCandidate(japan);
        when(countryService.getCountryByCode("XX"))
                .thenThrow(new br.usp.lab.oo.planejador_feriado.common.exception.ResourceNotFoundException(
                        "Country not found: XX"));

        RecommendationResponse response = engine.recommend(
                new RecommendationRequest(FROM, TO, List.of("JP", "XX"), null, 10));

        assertEquals(1, response.recommendations().size());
        assertEquals("XX", response.skipped().get(0).countryCode());
    }

    private void stubCandidate(Country country) {
        when(countryService.getCountryByCode(country.getIsoCode())).thenReturn(country);
        when(holidayService.getHolidaysInWindow(country.getIsoCode(), FROM, TO)).thenReturn(List.of());
    }

    private Country country(
            String name,
            String code,
            String currency,
            double latitude,
            double longitude) {
        return new Country(
                name,
                name,
                code,
                "Test",
                "Test",
                List.of(name + " City"),
                List.of("Language"),
                List.of(currency),
                List.of("UTC"),
                latitude,
                longitude);
    }
}
