package br.usp.lab.oo.planejador_feriado;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiContractTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void documentsStructuredSearchAndFrontendMetadata() throws Exception {
    mockMvc
      .perform(get("/v3/api-docs"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.info.version").value("v1"))
      .andExpect(jsonPath("$.paths['/api/v1/recommendations'].get").exists())
      .andExpect(jsonPath("$.paths['/api/v1/recommendations'].post").exists())
      .andExpect(jsonPath("$.paths['/api/v1/meta'].get").exists())
      .andExpect(
        jsonPath(
          "$.components.schemas.RecommendationSearchRequest.properties.origin"
        ).exists()
      );
  }
}
