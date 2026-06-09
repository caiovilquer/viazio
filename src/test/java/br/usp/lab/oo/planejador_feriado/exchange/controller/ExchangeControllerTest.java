package br.usp.lab.oo.planejador_feriado.exchange.controller;

import br.usp.lab.oo.planejador_feriado.common.exception.ExternalApiException;
import br.usp.lab.oo.planejador_feriado.common.exception.ResourceNotFoundException;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.exchange.service.ExchangeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExchangeController.class)
class ExchangeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExchangeService service;

    @Test
    void shouldReturnExchangeRate() throws Exception {
        when(service.getExchangeRate("USD")).thenReturn(new Exchange("USD", 5.12));

        mockMvc.perform(get("/api/exchange/USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.valueInReais").value(5.12));
    }

    @Test
    void shouldReturn404WhenCurrencyUnavailable() throws Exception {
        when(service.getExchangeRate("ZZZ"))
                .thenThrow(new ResourceNotFoundException("Câmbio não encontrado: ZZZ"));

        mockMvc.perform(get("/api/exchange/ZZZ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Câmbio não encontrado: ZZZ"));
    }

    @Test
    void shouldReturn502WhenUpstreamFails() throws Exception {
        when(service.getExchangeRate("USD"))
                .thenThrow(new ExternalApiException("Falha ao consultar serviço de câmbio", new RuntimeException()));

        mockMvc.perform(get("/api/exchange/USD"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));
    }
}
