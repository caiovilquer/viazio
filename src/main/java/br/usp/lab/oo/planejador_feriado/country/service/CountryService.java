package br.usp.lab.oo.planejador_feriado.country.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import br.usp.lab.oo.planejador_feriado.country.client.RestCountriesClient;
import br.usp.lab.oo.planejador_feriado.country.dto.CountryDTO;
import br.usp.lab.oo.planejador_feriado.country.model.Country;

@Service
public class CountryService {

    private final RestCountriesClient client;

    public CountryService(RestCountriesClient client) {
        this.client = client;
    }

    public Country getCountryByName(String name) {
        List<CountryDTO> responseList;
        try {
            responseList = client.getCountryByName(name);
        } catch (RestClientException e) {
            throw new RuntimeException("Country not found");
        }

        if (responseList == null || responseList.isEmpty()) {
            throw new RuntimeException("Country not found");
        }

        return toModel(responseList.get(0));
    }

    public Country getCountryByCode(String code) {
        List<CountryDTO> responseList;
        try {
            responseList = client.getCountryByCode(code);
        } catch (RestClientException e) {
            throw new RuntimeException("Country not found");
        }

        if (responseList == null || responseList.isEmpty()) {
            throw new RuntimeException("Country not found");
        }

        return toModel(responseList.get(0));
    }

    private Country toModel(CountryDTO dto) {
        return new Country(
            dto.name().common(),
            dto.isoCode(),
            dto.region(),
            dto.subregion(),
            dto.capital(),
            dto.languages() != null ?
                new ArrayList<>(dto.languages().values()) :
                List.of(),
            dto.currencies() != null ?
                new ArrayList<>(dto.currencies().keySet()) :
                List.of(),
            dto.timezones()
        );
    }
}
