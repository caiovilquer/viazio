package br.usp.lab.oo.planejador_feriado.meta.dto;

import java.util.List;

public record CountryOption(
        String code,
        String name,
        String flagEmoji,
        String region,
        String subregion,
        String defaultCity,
        List<CityOption> cities
) {
    public CountryOption {
        cities = cities != null ? List.copyOf(cities) : List.of();
    }
}
