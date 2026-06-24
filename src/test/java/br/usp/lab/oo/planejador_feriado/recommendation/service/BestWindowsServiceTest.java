package br.usp.lab.oo.planejador_feriado.recommendation.service;

import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.holiday.service.HolidayService;
import br.usp.lab.oo.planejador_feriado.recommendation.detector.LongWeekendDetector;
import br.usp.lab.oo.planejador_feriado.recommendation.dto.BestWindowsResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.model.BestWindowsRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.weight.ResolvedWeights;
import br.usp.lab.oo.planejador_feriado.recommendation.weight.WeightResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BestWindowsServiceTest {

    @Mock
    private HolidayService holidayService;
    @Mock
    private TravelRecommendationEngine recommendationEngine;
    @Mock
    private WeightResolver weightResolver;

    private BestWindowsService service;

    @BeforeEach
    void setUp() {
        LongWeekendDetector detector = new LongWeekendDetector();
        service = new BestWindowsService(
                holidayService,
                detector,
                new TravelWindowEvaluator(detector),
                recommendationEngine,
                weightResolver);
    }

    @Test
    void findsLongWeekendsInPeriodWithoutCandidates() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);

        // Corpus Christi 2026-06-04 cai numa quinta → ponte na sexta → feriadão de 4 dias.
        Holiday corpusChristi = new Holiday(
                LocalDate.of(2026, 6, 4), "Corpus Christi", "Corpus Christi", List.of("Public"));
        when(holidayService.getHolidaysInWindow(eq("BR"), eq(null), eq(from), eq(to)))
                .thenReturn(List.of(corpusChristi));
        when(weightResolver.resolve(any(), any())).thenReturn(new ResolvedWeights("padrão", Map.of()));

        BestWindowsRequest request = new BestWindowsRequest(
                from, to, 3, 6, List.of(), null, 3, null, Map.of(), List.of(),
                "BR", null, null, null);

        BestWindowsResponse response = service.findBestWindows(request);

        assertEquals("padrão", response.profile());
        assertFalse(response.windows().isEmpty());
        assertEquals(4, response.windows().get(0).totalDays());
        assertTrue(response.windows().get(0).label().contains("Feriadão de 4 dias"));
        assertTrue(response.windows().get(0).topDestinations().isEmpty());
    }
}
