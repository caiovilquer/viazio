package br.usp.lab.oo.planejador_feriado;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.exchange.service.ExchangeService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ExchangeServiceIntegrationTest {

    @Autowired
    private ExchangeService service;

    @Test
    void testaBuscaDeCotacaoDolar() {
        Exchange cotacao = service.getExchangeRate("USD");

        assertNotNull(cotacao, "A cotação não deveria ser nula");
        assertEquals("USD", cotacao.getCurrency(), "A moeda deveria ser USD");                          //verifica moeda
        assertTrue(cotacao.getValueInReais() > 0, "O valor de conversão deve ser maior que zero");      //valor avlido
    }

    @Test
    void testaBuscaDeCotacaoEuro() {
        Exchange cotacao = service.getExchangeRate("EUR");

        assertNotNull(cotacao);
        assertEquals("EUR", cotacao.getCurrency());
        assertTrue(cotacao.getValueInReais() > 0);
    }
}