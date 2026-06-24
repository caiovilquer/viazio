package br.usp.lab.oo.planejador_feriado.recommendation.controller;

import br.usp.lab.oo.planejador_feriado.recommendation.dto.BestWindowsResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.dto.RecommendationResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.dto.WindowSuggestion;
import br.usp.lab.oo.planejador_feriado.destination.model.DestinationCity;
import br.usp.lab.oo.planejador_feriado.recommendation.model.DataQuality;
import br.usp.lab.oo.planejador_feriado.recommendation.model.GroundCostEstimate;
import br.usp.lab.oo.planejador_feriado.recommendation.model.OriginReference;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoredCriterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TravelEffort;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TravelRecommendation;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TripFeasibility;
import br.usp.lab.oo.planejador_feriado.recommendation.model.WindowAssessment;
import br.usp.lab.oo.planejador_feriado.recommendation.service.BestWindowsService;
import br.usp.lab.oo.planejador_feriado.recommendation.service.TravelRecommendationEngine;
import br.usp.lab.oo.planejador_feriado.recommendation.weight.WeightResolver;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TravelRecommendationEngine recommendationEngine;

    @MockitoBean
    private BestWindowsService bestWindowsService;

    @MockitoBean
    private WeightResolver weightResolver;

    @Test
    void shouldReturnRecommendationsForValidRequest() throws Exception {
        RecommendationResponse response = new RecommendationResponse(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                Instant.parse("2026-06-01T00:00:00Z"),
                new OriginReference("BR", null, -10.0, -55.0),
                "padrão",
                Map.of("weather", 0.30, "cost", 0.30),
                new WindowAssessment(70.0, 30, 10, 20, List.of(), "10 de 30 dias livres"),
                List.of(new TravelRecommendation(
                        "JP",
                        "Japan",
                        68.0,
                        70.0,
                        65.0,
                        new DataQuality(0.75, 75.0, 3, 4, List.of("cost")),
                        List.of(new ScoredCriterion("weather", "Clima", "☀️", true, 80.0, 0.30, 24.0, "ok")),
                        List.of("clima agradável"),
                        List.of("Custo: dado indisponível"),
                        "Japan — nota de viagem 65: clima agradável",
                        null,
                        null,
                        new TripFeasibility(
                                new DestinationCity("JP", "Tokyo", 35.68, 139.76, List.of(9.0), true),
                                new TravelEffort(17_600.0, 20.6, 28.1, -3.0, 9.0, 12.0, "LONG", true),
                                new GroundCostEstimate(
                                        "BRL", 500.0, 5_000.0, 1, 10, 1.4,
                                        "2024", "2024", "LOW", "PPP"),
                                List.of("passagens aéreas"))
                )),
                List.of()
        );

        when(recommendationEngine.recommend(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/recommendations")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .param("countries", "JP,FR")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile").value("padrão"))
                .andExpect(jsonPath("$.recommendations[0].countryCode").value("JP"))
                .andExpect(jsonPath("$.recommendations[0].tripScore").value(65.0))
                .andExpect(jsonPath("$.recommendations[0].dataQuality.confidenceScore").value(75.0))
                .andExpect(jsonPath("$.recommendations[0].highlights[0]").value("clima agradável"))
                .andExpect(jsonPath("$.recommendations[0].breakdown[0].label").value("Clima"))
                .andExpect(jsonPath("$.recommendations[0].feasibility.destination.name").value("Tokyo"))
                .andExpect(jsonPath("$.recommendations[0].feasibility.groundCost.estimatedTotal").value(5000.0))
                .andExpect(jsonPath("$.recommendations[0].feasibility.notIncluded[0]").value("passagens aéreas"));
    }

    @Test
    void shouldReturnBadRequestWhenWindowInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations")
                        .param("from", "2026-06-30")
                        .param("to", "2026-06-01")
                        .param("countries", "JP"))
                .andExpect(status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("'from' deve ser anterior ou igual a 'to'"))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    void shouldReturnBadRequestWhenBothCountriesAndRegionProvided() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .param("countries", "JP")
                        .param("region", "Europe"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenNeitherCountriesNorRegionProvided() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUnknownProfile() throws Exception {
        when(weightResolver.isKnownProfile("inexistente")).thenReturn(false);
        when(weightResolver.availableProfiles()).thenReturn(List.of("economico", "clima-perfeito"));

        mockMvc.perform(get("/api/v1/recommendations")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .param("countries", "JP")
                        .param("profile", "inexistente"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidTravelersAndGroundBudget() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .param("countries", "JP")
                        .param("travelers", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/recommendations")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .param("countries", "JP")
                        .param("maxGroundBudget", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAcceptStructuredPostRequestAndNormalizeIt() throws Exception {
        when(recommendationEngine.recommend(any())).thenReturn(emptyResponse());

        mockMvc.perform(post("/api/v1/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "from": "2026-09-04",
                                  "to": "2026-09-07",
                                  "countries": ["jp", "FR", "jp"],
                                  "limit": 5,
                                  "weights": {"weather": 0.4, "cost": 0.3},
                                  "exclude": ["us"],
                                  "origin": {
                                    "countryCode": "br",
                                    "subdivisionCode": "br-sp",
                                    "city": "São Paulo",
                                    "latitude": -23.5505,
                                    "longitude": -46.6333
                                  },
                                  "travelers": 2,
                                  "maxGroundBudgetBrl": 5000
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<RecommendationRequest> captor = ArgumentCaptor.forClass(RecommendationRequest.class);
        verify(recommendationEngine).recommend(captor.capture());
        RecommendationRequest request = captor.getValue();
        assertEquals(List.of("JP", "FR"), request.countryCodes());
        assertEquals("BR", request.originCountryCode());
        assertEquals("BR-SP", request.originSubdivisionCode());
        assertEquals("São Paulo", request.originCityName());
        assertEquals(2, request.travelers());
        assertEquals(5_000.0, request.maxGroundBudgetBrl());
        assertTrue(request.weightOverrides().containsKey(
                br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion.WEATHER));
    }

    @Test
    void shouldReturnFieldViolationsForInvalidPostBody() throws Exception {
        mockMvc.perform(post("/api/v1/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "from": "2026-09-04",
                                  "to": "2026-09-07",
                                  "countries": ["JAPAN"],
                                  "travelers": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations[?(@.field == 'countries[0]')]").exists())
                .andExpect(jsonPath("$.violations[?(@.field == 'travelers')]").exists());
    }

    @Test
    void shouldRejectAmbiguousCandidateSelectionInPostBody() throws Exception {
        mockMvc.perform(post("/api/v1/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "from": "2026-09-04",
                                  "to": "2026-09-07",
                                  "countries": ["JP"],
                                  "region": "Asia"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void shouldReturnBestWindows() throws Exception {
        BestWindowsResponse response = new BestWindowsResponse(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                "padrão",
                List.of(new WindowSuggestion(
                        LocalDate.of(2026, 2, 14),
                        LocalDate.of(2026, 2, 18),
                        5,
                        1,
                        1,
                        "Feriadão de 5 dias (Carnaval + ponte)",
                        98.0,
                        List.of()
                ))
        );

        when(bestWindowsService.findBestWindows(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/recommendations/best-windows")
                        .param("from", "2026-01-01")
                        .param("to", "2026-12-31")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.windows[0].totalDays").value(5))
                .andExpect(jsonPath("$.windows[0].label").value("Feriadão de 5 dias (Carnaval + ponte)"));
    }

    private RecommendationResponse emptyResponse() {
        return new RecommendationResponse(
                LocalDate.of(2026, 9, 4),
                LocalDate.of(2026, 9, 7),
                Instant.parse("2026-06-23T00:00:00Z"),
                new OriginReference("BR", null, -15.79, -47.88, "Brasília"),
                "padrão",
                Map.of(),
                new WindowAssessment(50.0, 4, 2, 2, List.of(), "janela comum"),
                List.of(),
                List.of());
    }
}
