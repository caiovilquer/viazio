package br.usp.lab.oo.planejador_feriado.recommendation.controller;

import br.usp.lab.oo.planejador_feriado.recommendation.dto.RecommendationResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TravelRecommendation;
import br.usp.lab.oo.planejador_feriado.recommendation.service.TravelRecommendationEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

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

    @Test
    void shouldReturnRecommendationsForValidRequest() throws Exception {
        RecommendationResponse response = new RecommendationResponse(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                List.of(),
                List.of(new TravelRecommendation(
                        "JP",
                        "Japan",
                        68.0,
                        List.of(new ScoreEntry("CAMBIO", 35, 35, "ok")),
                        "JP — score 68: câmbio muito favorável"
                )),
                List.of()
        );

        when(recommendationEngine.recommend(any())).thenReturn(response);

        mockMvc.perform(get("/api/recommendations")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .param("countries", "JP,FR")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations[0].countryCode").value("JP"))
                .andExpect(jsonPath("$.recommendations[0].score").value(68.0));
    }

    @Test
    void shouldReturnBadRequestWhenWindowInvalid() throws Exception {
        mockMvc.perform(get("/api/recommendations")
                        .param("from", "2026-06-30")
                        .param("to", "2026-06-01")
                        .param("countries", "JP"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenBothCountriesAndRegionProvided() throws Exception {
        mockMvc.perform(get("/api/recommendations")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .param("countries", "JP")
                        .param("region", "Europe"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenNeitherCountriesNorRegionProvided() throws Exception {
        mockMvc.perform(get("/api/recommendations")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30"))
                .andExpect(status().isBadRequest());
    }
}
