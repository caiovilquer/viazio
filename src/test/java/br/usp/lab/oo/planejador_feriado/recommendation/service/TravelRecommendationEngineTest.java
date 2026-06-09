package br.usp.lab.oo.planejador_feriado.recommendation.service;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.exchange.service.ExchangeService;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.holiday.service.HolidayService;
import br.usp.lab.oo.planejador_feriado.recommendation.detector.LongWeekendDetector;
import br.usp.lab.oo.planejador_feriado.recommendation.dto.RecommendationResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TravelRecommendation;
import br.usp.lab.oo.planejador_feriado.recommendation.strategy.ExchangeRateStrategy;
import br.usp.lab.oo.planejador_feriado.recommendation.strategy.FreeDaysRatioStrategy;
import br.usp.lab.oo.planejador_feriado.recommendation.strategy.HolidayWindowStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TravelRecommendationEngineTest {

    @Mock
    private CountryService countryService;

    @Mock
    private HolidayService holidayService;

    @Mock
    private ExchangeService exchangeService;

    private TravelRecommendationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TravelRecommendationEngine(
                countryService,
                holidayService,
                exchangeService,
                List.of(
                        new HolidayWindowStrategy(),
                        new ExchangeRateStrategy(),
                        new FreeDaysRatioStrategy()
                ),
                new LongWeekendDetector()
        );
    }

    @Test
    void shouldRankJapanAboveFranceForJune2026() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);
        Holiday corpusChristi = new Holiday(
                LocalDate.of(2026, 6, 4),
                "Corpus Christi",
                "Corpus Christi",
                List.of("Public")
        );

        Country japan = new Country("Japan", "JP", "Asia", "Eastern Asia",
                List.of("Tokyo"), List.of("Japanese"), List.of("JPY"), List.of("UTC+09:00"));
        Country france = new Country("France", "FR", "Europe", "Western Europe",
                List.of("Paris"), List.of("French"), List.of("EUR"), List.of("UTC+01:00"));

        when(holidayService.getHolidaysInWindow(eq("BR"), eq(from), eq(to)))
                .thenReturn(List.of(corpusChristi));
        when(countryService.getCountryByCode("JP")).thenReturn(japan);
        when(countryService.getCountryByCode("FR")).thenReturn(france);
        when(holidayService.getHolidaysInWindow(eq("JP"), eq(from), eq(to))).thenReturn(List.of());
        when(holidayService.getHolidaysInWindow(eq("FR"), eq(from), eq(to))).thenReturn(List.of());
        when(exchangeService.getExchangeRate("JPY")).thenReturn(new Exchange("JPY", 0.035));
        when(exchangeService.getExchangeRate("EUR")).thenReturn(new Exchange("EUR", 6.30));

        RecommendationRequest request = new RecommendationRequest(from, to, List.of("JP", "FR"), null, null, 10);
        RecommendationResponse response = engine.recommend(request);

        assertEquals(2, response.recommendations().size());
        TravelRecommendation first = response.recommendations().get(0);
        TravelRecommendation second = response.recommendations().get(1);

        assertEquals("JP", first.countryCode());
        assertEquals("FR", second.countryCode());
        assertTrue(first.score() > second.score());
        assertFalse(response.brazilLongWeekends().isEmpty());
        assertTrue(first.summary().contains("JP"));
    }

    @Test
    void shouldSkipInvalidCandidateWithoutFailingRanking() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);

        Country japan = new Country("Japan", "JP", "Asia", "Eastern Asia",
                List.of("Tokyo"), List.of("Japanese"), List.of("JPY"), List.of("UTC+09:00"));

        when(holidayService.getHolidaysInWindow(eq("BR"), eq(from), eq(to))).thenReturn(List.of());
        when(countryService.getCountryByCode("JP")).thenReturn(japan);
        when(countryService.getCountryByCode("XX")).thenThrow(new RuntimeException("Country not found"));
        when(holidayService.getHolidaysInWindow(eq("JP"), eq(from), eq(to))).thenReturn(List.of());
        when(exchangeService.getExchangeRate("JPY")).thenReturn(new Exchange("JPY", 0.035));

        RecommendationRequest request = new RecommendationRequest(from, to, List.of("JP", "XX"), null, null, 10);
        RecommendationResponse response = engine.recommend(request);

        assertEquals(1, response.recommendations().size());
        assertEquals(1, response.skipped().size());
        assertEquals("XX", response.skipped().get(0).countryCode());
    }

    @Test
    void shouldResolveCandidatesByRegionWithLimit() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 10);

        Country france = new Country("France", "FR", "Europe", "Western Europe",
                List.of("Paris"), List.of("French"), List.of("EUR"), List.of("UTC+01:00"));
        Country italy = new Country("Italy", "IT", "Europe", "Southern Europe",
                List.of("Rome"), List.of("Italian"), List.of("EUR"), List.of("UTC+01:00"));

        when(countryService.getCountriesByRegion("Europe", 2)).thenReturn(List.of(france, italy));
        when(countryService.getCountryByCode("FR")).thenReturn(france);
        when(countryService.getCountryByCode("IT")).thenReturn(italy);
        when(holidayService.getHolidaysInWindow(anyString(), eq(from), eq(to))).thenReturn(List.of());
        when(exchangeService.getExchangeRate("EUR")).thenReturn(new Exchange("EUR", 6.30));

        RecommendationRequest request = new RecommendationRequest(from, to, List.of(), "Europe", null, 2);
        RecommendationResponse response = engine.recommend(request);

        assertEquals(2, response.recommendations().size());
    }
}
