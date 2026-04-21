package br.usp.lab.oo.planejador_feriado.country.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.usp.lab.oo.planejador_feriado.country.dto.CountryDTO;

import java.util.List;

@Component
public class RestCountriesClient {

    private final RestClient restClient;

    public RestCountriesClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://restcountries.com/v3.1")
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
}