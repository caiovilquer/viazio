package br.usp.lab.oo.planejador_feriado.recommendation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
class RecommendationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnRankedRecommendationsFromRealApis() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .param("countries", "JP,FR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.recommendations.length()").value(2))
                .andExpect(jsonPath("$.recommendations[0].tripScore").isNumber())
                .andExpect(jsonPath("$.recommendations[0].summary").isString());
    }
}
