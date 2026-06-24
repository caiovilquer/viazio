package br.usp.lab.oo.planejador_feriado.country.client;

import br.usp.lab.oo.planejador_feriado.country.dto.CountryDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Fonte de dados de países a partir de um dataset estático embarcado
 * ({@code resources/data/countries.json}, derivado do projeto MIT mledoze/countries).
 *
 * <p>A API gratuita do RestCountries v3.1 foi descontinuada (passou a exigir chave).
 * Como os dados de países praticamente não mudam, embarcá-los torna a aplicação
 * independente de rede para essa informação: respostas instantâneas, sem timeouts,
 * cota ou indisponibilidade — mais adequado a um produto real e a demonstrações.</p>
 */
@Component
public class StaticCountryClient implements CountryClient {

    private final List<CountryDTO> countries;
    private final Map<String, CountryDTO> byCode;

    public StaticCountryClient(ObjectMapper objectMapper) {
        this.countries = List.copyOf(load(objectMapper));
        this.byCode = countries.stream()
                .filter(country -> country.isoCode() != null)
                .collect(Collectors.toMap(
                        country -> country.isoCode().toUpperCase(Locale.ROOT),
                        Function.identity(),
                        (a, b) -> a));
    }

    private List<CountryDTO> load(ObjectMapper objectMapper) {
        try (InputStream input = new ClassPathResource("data/countries.json").getInputStream()) {
            return objectMapper.readValue(input, new TypeReference<List<CountryDTO>>() {});
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao carregar o dataset de países", e);
        }
    }

    @Override
    public List<CountryDTO> getAllCountries() {
        return countries;
    }

    @Override
    public List<CountryDTO> getCountryByCode(String countryCode) {
        CountryDTO match = byCode.get(countryCode.toUpperCase(Locale.ROOT));
        return match != null ? List.of(match) : List.of();
    }

    @Override
    public List<CountryDTO> getCountryByName(String countryName) {
        String query = countryName.trim().toLowerCase(Locale.ROOT);
        List<CountryDTO> exact = countries.stream()
                .filter(country -> commonName(country).equals(query))
                .toList();
        if (!exact.isEmpty()) {
            return exact;
        }
        return countries.stream()
                .filter(country -> commonName(country).contains(query))
                .toList();
    }

    @Override
    public List<CountryDTO> getCountriesByRegion(String region) {
        String query = region.trim().toLowerCase(Locale.ROOT);
        return countries.stream()
                .filter(country -> country.region() != null
                        && country.region().toLowerCase(Locale.ROOT).equals(query))
                .toList();
    }

    private String commonName(CountryDTO country) {
        return country.name() != null && country.name().common() != null
                ? country.name().common().toLowerCase(Locale.ROOT)
                : "";
    }
}
