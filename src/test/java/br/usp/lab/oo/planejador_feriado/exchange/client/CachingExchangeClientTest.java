package br.usp.lab.oo.planejador_feriado.exchange.client;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.usp.lab.oo.planejador_feriado.exchange.dto.ExchangeDTO;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CachingExchangeClientTest {

  @Mock
  private AwesomeApiClient delegate;

  @Test
  void cachesRepeatedCallsByCurrency() {
    Map<String, ExchangeDTO> usd = Map.of(
      "USDBRL",
      new ExchangeDTO("USD", 5.0)
    );
    when(delegate.getExchangeRate("USD")).thenReturn(usd);

    CachingExchangeClient client = new CachingExchangeClient(delegate);

    Map<String, ExchangeDTO> first = client.getExchangeRate("USD");
    Map<String, ExchangeDTO> second = client.getExchangeRate("usd");

    assertSame(usd, first);
    assertSame(usd, second);
    verify(delegate, times(1)).getExchangeRate("USD");
  }
}
