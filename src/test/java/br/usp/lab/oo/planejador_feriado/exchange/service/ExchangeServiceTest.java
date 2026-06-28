package br.usp.lab.oo.planejador_feriado.exchange.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.usp.lab.oo.planejador_feriado.common.exception.ExternalApiException;
import br.usp.lab.oo.planejador_feriado.common.exception.ResourceNotFoundException;
import br.usp.lab.oo.planejador_feriado.exchange.client.AwesomeApiClient;
import br.usp.lab.oo.planejador_feriado.exchange.dto.ExchangeDTO;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceTest {

  @Mock
  private AwesomeApiClient client;

  @InjectMocks
  private ExchangeService service;

  @Test
  void shouldReturnRateForValidCurrency() {
    when(client.getExchangeRate("USD")).thenReturn(
      Map.of("USDBRL", new ExchangeDTO("USD", 5.12))
    );

    Exchange exchange = service.getExchangeRate("USD");

    assertEquals("USD", exchange.getCurrency());
    assertEquals(5.12, exchange.getValueInReais());
    verify(client).getExchangeRate("USD");
  }

  @Test
  void shouldUppercaseCurrencyBeforeQuerying() {
    when(client.getExchangeRate("EUR")).thenReturn(
      Map.of("EURBRL", new ExchangeDTO("EUR", 6.30))
    );

    Exchange exchange = service.getExchangeRate("eur");

    assertEquals("EUR", exchange.getCurrency());
    verify(client).getExchangeRate("EUR");
  }

  @Test
  void shouldShortCircuitForBrlWithoutCallingClient() {
    Exchange exchange = service.getExchangeRate("BRL");

    assertEquals("BRL", exchange.getCurrency());
    assertEquals(1.00, exchange.getValueInReais());
    verify(client, never()).getExchangeRate("BRL");
  }

  @Test
  void shouldThrowNotFoundWhenResponseMissingExpectedKey() {
    when(client.getExchangeRate("USD")).thenReturn(Map.of());

    ResourceNotFoundException ex = assertThrows(
      ResourceNotFoundException.class,
      () -> service.getExchangeRate("USD")
    );
    assertEquals("Câmbio não encontrado: USD", ex.getMessage());
  }

  @Test
  void shouldThrowNotFoundWhenResponseIsNull() {
    when(client.getExchangeRate("USD")).thenReturn(null);

    assertThrows(ResourceNotFoundException.class, () ->
      service.getExchangeRate("USD")
    );
  }

  @Test
  void shouldThrowNotFoundWhenUpstreamReturns404() {
    when(client.getExchangeRate("ZZZ")).thenThrow(
      HttpClientErrorException.create(
        HttpStatus.NOT_FOUND,
        "Not Found",
        HttpHeaders.EMPTY,
        new byte[0],
        null
      )
    );

    assertThrows(ResourceNotFoundException.class, () ->
      service.getExchangeRate("ZZZ")
    );
  }

  @Test
  void shouldThrowExternalApiWhenClientFails() {
    when(client.getExchangeRate("USD")).thenThrow(
      new RestClientException("offline")
    );

    assertThrows(ExternalApiException.class, () ->
      service.getExchangeRate("USD")
    );
  }
}
