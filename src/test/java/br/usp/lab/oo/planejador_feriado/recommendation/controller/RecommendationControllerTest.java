package br.usp.lab.oo.planejador_feriado.recommendation.controller;

import br.usp.lab.oo.planejador_feriado.recommendation.dto.BestWindowsResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.dto.RecommendationResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.dto.WindowSuggestion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoredCriterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TravelRecommendation;
import br.usp.lab.oo.planejador_feriado.recommendation.service.BestWindowsService;
import br.usp.lab.oo.planejador_feriado.recommendation.service.TravelRecommendationEngine;
import br.usp.lab.oo.planejador_feriado.recommendation.weight.WeightResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                "padrão",
                Map.of("exchange", 0.15, "weather", 0.20),
                List.of(),
                List.of(new TravelRecommendation(
                        "JP",
                        "Japan",
                        68.0,
                        List.of(new ScoredCriterion("exchange", "Câmbio", "💱", true, 100.0, 0.15, 15.0, "ok")),
                        List.of("câmbio favorável"),
                        "JP — score 68: câmbio favorável"
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
                .andExpect(jsonPath("$.recommendations[0].score").value(68.0))
                .andExpect(jsonPath("$.recommendations[0].highlights[0]").value("câmbio favorável"))
                .andExpect(jsonPath("$.recommendations[0].breakdown[0].label").value("Câmbio"));
    }

    @Test
    void shouldReturnBadRequestWhenWindowInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations")
                        .param("from", "2026-06-30")
                        .param("to", "2026-06-01")
                        .param("countries", "JP"))
                .andExpect(status().isBadRequest());
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
}
