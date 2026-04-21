package br.usp.lab.oo.planejador_feriado;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.exchange.service.ExchangeService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExchangeServiceIntegrationTest {

    @Autowired
    private ExchangeService service;

    @Test
    void shouldReturnExchangeForUsd() {
        Exchange cotacao = service.getExchangeRate("USD");

        assertNotNull(cotacao, "A cotação não deveria ser nula");
        assertEquals("USD", cotacao.getCurrency(), "A moeda retornada deve ser exatamente USD");
        
        assertTrue(cotacao.getValueInReais() > 0, "O valor deve ser maior que zero");
        assertTrue(cotacao.getValueInReais() < 15, "O valor do dólar está fora de uma margem real");
    }

    @Test
    void shouldReturnExchangeForEurWithLowercase() {
        Exchange cotacao = service.getExchangeRate("eur");

        assertNotNull(cotacao);
        assertEquals("EUR", cotacao.getCurrency(), "O serviço deve ter padronizado a sigla para maiúscula");
        assertTrue(cotacao.getValueInReais() > 0);
    }

    @Test
    void shouldThrowExceptionForInvalidCurrency() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.getExchangeRate("XYZ");
        });

        assertTrue(exception.getMessage().contains("Câmbio não encontrado"), 
                  "Deveria avisar que o câmbio não foi encontrado");
    }

    @Test
    void shouldFormatToStringCorrectly() {              //toString()
        Exchange cotacao = service.getExchangeRate("USD");
        String impressao = cotacao.toString();
        
        assertNotNull(impressao);
        assertTrue(impressao.startsWith("Câmbio: 1 USD = R$ "), "A formatação do toString está incorreta");
    }
}