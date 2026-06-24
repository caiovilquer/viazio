package br.usp.lab.oo.planejador_feriado.exchange.client;

import br.usp.lab.oo.planejador_feriado.common.config.ExternalApisProperties;
import br.usp.lab.oo.planejador_feriado.common.config.RestClientFactory;
import br.usp.lab.oo.planejador_feriado.exchange.dto.ExchangeDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class AwesomeApiClient implements ExchangeClient {

    private final RestClient restClient;

    public AwesomeApiClient(RestClientFactory restClientFactory, ExternalApisProperties properties) {
        this.restClient = restClientFactory.builderFor(properties.awesomeApi().baseUrl()).build();
    }

    @Override
    @Retry(name = "exchangeApi")
    @CircuitBreaker(name = "exchangeApi")
    @Bulkhead(name = "exchangeApi")
    public Map<String, ExchangeDTO> getExchangeRate(String currencyCode) {
        return restClient.get()                                           //Ex: https://economia.awesomeapi.com.br/json/last/USD-BRL
                .uri("/last/" + currencyCode + "-BRL")
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, ExchangeDTO>>() {});
    }
}
