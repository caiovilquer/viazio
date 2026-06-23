package br.usp.lab.oo.planejador_feriado.exchange.client;

import br.usp.lab.oo.planejador_feriado.common.config.ExternalApisProperties;
import br.usp.lab.oo.planejador_feriado.exchange.dto.ExchangeDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class AwesomeApiClient {

    private final RestClient restClient;

    public AwesomeApiClient(ExternalApisProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.awesomeApi().baseUrl())
                .build();
    }

    public Map<String, ExchangeDTO> getExchangeRate(String currencyCode) {
        return restClient.get()                                           //Ex: https://economia.awesomeapi.com.br/json/last/USD-BRL
                .uri("/last/" + currencyCode + "-BRL")
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, ExchangeDTO>>() {});
    }
}