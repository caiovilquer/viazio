package br.usp.lab.oo.planejador_feriado.country.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.usp.lab.oo.planejador_feriado.common.config.ExternalApisProperties;
import br.usp.lab.oo.planejador_feriado.country.dto.CountryDTO;

import java.util.List;

@Component
public class RestCountriesClient {

    private final RestClient restClient;

    public RestCountriesClient(ExternalApisProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.restCountries().baseUrl())
                .build();
    }

    public List<CountryDTO> getCountryByCode(String countryCode) {
        return restClient.get()
                .uri("/alpha/" + countryCode)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CountryDTO>>() {});
    }

    public List<CountryDTO> getCountryByName(String countryName) {
        return restClient.get()
                .uri("/name/" + countryName)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CountryDTO>>() {});
    }

    public List<CountryDTO> getCountriesByRegion(String region) {
        return restClient.get()
                .uri("/region/" + region)
                .retrieve()
                .body(new ParameterizedTypeReference<List<CountryDTO>>() {});
    }
}