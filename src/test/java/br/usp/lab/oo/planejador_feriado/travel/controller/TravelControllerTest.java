package br.usp.lab.oo.planejador_feriado.travel.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.usp.lab.oo.planejador_feriado.common.exception.ResourceNotFoundException;
import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.travel.model.TravelOverview;
import br.usp.lab.oo.planejador_feriado.travel.service.TravelService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TravelController.class)
class TravelControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TravelService travelService;

  @Test
  void shouldReturnOverviewForCountryCode() throws Exception {
    Country japan = new Country(
      "Japan",
      "JP",
      "Asia",
      "Eastern Asia",
      List.of("Tokyo"),
      List.of("Japanese"),
      List.of("JPY"),
      List.of("UTC+09:00")
    );
    TravelOverview overview = new TravelOverview(
      japan,
      List.of(),
      new Exchange("JPY", 0.035),
      null
    );

    when(travelService.getOverviewByCountryCode("JP")).thenReturn(overview);

    mockMvc
      .perform(get("/api/v1/travel/JP"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.country.name").value("Japan"))
      .andExpect(jsonPath("$.exchangeToBrl.currency").value("JPY"));
  }

  @Test
  void shouldReturn404WhenCountryNotFound() throws Exception {
    when(travelService.getOverviewByCountryCode("ZZ")).thenThrow(
      new ResourceNotFoundException("Country not found: ZZ")
    );

    mockMvc
      .perform(get("/api/v1/travel/ZZ"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.path").value("/api/v1/travel/ZZ"));
  }
}
