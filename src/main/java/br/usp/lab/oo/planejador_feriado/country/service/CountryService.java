package br.usp.lab.oo.planejador_feriado.country.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

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
        var responseList = client.getCountryByName(name); // var = CountryDTO[]
        
        if (responseList == null || responseList.isEmpty())
            throw new RuntimeException("Country not found");
        
        CountryDTO dto = responseList.get(0);
        return toModel(dto);
    }

    public Country getCountryByCode(String code) {
        var responseList = client.getCountryByCode(code);

        if (responseList == null || responseList.isEmpty()) 
            throw new RuntimeException("Country not found");

        CountryDTO dto = responseList.get(0);
        return toModel(dto);
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