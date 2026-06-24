package br.usp.lab.oo.planejador_feriado.country.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CountryDTO(
    NameDTO name,
    @JsonProperty("cca2")
    String isoCode, // cca2
    String region,
    String subregion,
    List<String> capital,
    Map<String, String> languages,
    Map<String, CurrencyDTO> currencies,
    List<String> timezones,
    List<Double> latlng // [latitude, longitude] do centroide do país
) {
    public record NameDTO(String common, Map<String, TranslationDTO> translations) {}
    public record TranslationDTO(String common) {}
    public record CurrencyDTO(String name, String symbol) {}
}