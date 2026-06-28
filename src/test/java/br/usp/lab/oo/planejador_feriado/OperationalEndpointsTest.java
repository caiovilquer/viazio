package br.usp.lab.oo.planejador_feriado;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.test.observability.auto-configure=true")
@AutoConfigureMockMvc
class OperationalEndpointsTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void exposesHealthAndPrometheusWithoutSensitiveHealthDetails()
    throws Exception {
    mockMvc
      .perform(get("/actuator/health"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("UP"))
      .andExpect(jsonPath("$.components").doesNotExist());

    mockMvc
      .perform(get("/actuator/prometheus"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("jvm_memory_used_bytes")));
  }

  @Test
  void correlatesResponseHeaderAndErrorBodyAndAddsSecurityHeaders()
    throws Exception {
    mockMvc
      .perform(
        post("/api/v1/recommendations")
          .header("X-Request-Id", "contract-test-42")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{}")
      )
      .andExpect(status().isBadRequest())
      .andExpect(header().string("X-Trace-Id", "contract-test-42"))
      .andExpect(header().string("X-Content-Type-Options", "nosniff"))
      .andExpect(header().string("X-Frame-Options", "DENY"))
      .andExpect(jsonPath("$.traceId").value("contract-test-42"))
      .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }
}
