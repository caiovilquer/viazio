package br.usp.lab.oo.planejador_feriado.country.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import br.usp.lab.oo.planejador_feriado.common.exception.ExternalApiException;
import br.usp.lab.oo.planejador_feriado.common.exception.ResourceNotFoundException;
import br.usp.lab.oo.planejador_feriado.country.client.CountryClient;
import br.usp.lab.oo.planejador_feriado.country.dto.CountryDTO;
import br.usp.lab.oo.planejador_feriado.country.model.Country;

@Service
public class CountryService {

    private final CountryClient client;

    public CountryService(CountryClient client) {
        this.client = client;
    }

    public Country getCountryByName(String name) {
        List<CountryDTO> responseList;
        try {
            responseList = client.getCountryByName(name);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Country not found: " + name);
        } catch (RestClientException e) {
            throw new ExternalApiException("Falha ao consultar serviço de países", e);
        }

        if (responseList == null || responseList.isEmpty()) {
            throw new ResourceNotFoundException("Country not found");
        }

        return toModel(responseList.get(0));
    }

    public Country getCountryByCode(String code) {
        List<CountryDTO> responseList;
        try {
            responseList = client.getCountryByCode(code);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Country not found: " + code);
        } catch (RestClientException e) {
            throw new ExternalApiException("Falha ao consultar serviço de países", e);
        }

        if (responseList == null || responseList.isEmpty()) {
            throw new ResourceNotFoundException("Country not found");
        }

        return toModel(responseList.get(0));
    }

    public Country getCountryByQuery(String query) {
        String trimmed = query.trim();
        if (looksLikeIsoCode(trimmed)) {
            return getCountryByCode(trimmed);
        }
        return getCountryByName(trimmed);
    }

    public static boolean looksLikeIsoCode(String query) {
        String trimmed = query.trim();
        return trimmed.length() == 2 && trimmed.matches("[A-Za-z]{2}");
    }

    public List<Country> getCountriesByRegion(String region, int limit) {
        List<CountryDTO> responseList;
        try {
            responseList = client.getCountriesByRegion(region);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Region not found");
        } catch (RestClientException e) {
            throw new ExternalApiException("Falha ao consultar serviço de países", e);
        }

        if (responseList == null || responseList.isEmpty()) {
            throw new ResourceNotFoundException("Region not found");
        }

        return responseList.stream()
                .limit(limit)
                .map(this::toModel)
                .toList();
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
