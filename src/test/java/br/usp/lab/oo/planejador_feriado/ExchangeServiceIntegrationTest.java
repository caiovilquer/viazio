package br.usp.lab.oo.planejador_feriado;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.exchange.service.ExchangeService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Tag("integration")
class ExchangeServiceIntegrationTest {

  @Autowired
  private ExchangeService service;

  @Test
  void shouldReturnExchangeForUsd() {
    Exchange cotacao = service.getExchangeRate("USD");

    assertNotNull(cotacao);
    assertEquals("USD", cotacao.getCurrency());
    assertTrue(cotacao.getValueInReais() > 0);
  }
}
