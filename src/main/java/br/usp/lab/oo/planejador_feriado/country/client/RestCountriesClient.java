package br.usp.lab.oo.planejador_feriado.country.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.usp.lab.oo.planejador_feriado.common.config.ExternalApisProperties;
import br.usp.lab.oo.planejador_feriado.common.config.RestClientFactory;
import br.usp.lab.oo.planejador_feriado.country.dto.CountryDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.util.List;

@Component
public class RestCountriesClient implements CountryClient {

    private final RestClient restClient;

    public RestCountriesClient(RestClientFactory restClientFactory, ExternalApisProperties properties) {
        this.restClient = restClientFactory.builderFor(properties.restCountries().baseUrl()).build();
    }

    @Override
    @Retry(name = "externalApi")
    @CircuitBreaker(name = "externalApi")
    public List<CountryDTO> getCountryByCode(String countryCode) {
        return restClient.get()
                .uri("/alpha/" + countryCode)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CountryDTO>>() {});
    }

    @Override
    @Retry(name = "externalApi")
    @CircuitBreaker(name = "externalApi")
    public List<CountryDTO> getCountryByName(String countryName) {
        return restClient.get()
                .uri("/name/" + countryName)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CountryDTO>>() {});
    }

    @Override
    @Retry(name = "externalApi")
    @CircuitBreaker(name = "externalApi")
    public List<CountryDTO> getCountriesByRegion(String region) {
        return restClient.get()
                .uri("/region/" + region)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CountryDTO>>() {});
    }
}
