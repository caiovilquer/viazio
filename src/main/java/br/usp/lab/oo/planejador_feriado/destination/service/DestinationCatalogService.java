package br.usp.lab.oo.planejador_feriado.destination.service;

import br.usp.lab.oo.planejador_feriado.common.exception.ResourceNotFoundException;
import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.destination.client.StaticDestinationCatalog;
import br.usp.lab.oo.planejador_feriado.destination.model.DestinationCity;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class DestinationCatalogService {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

    private final StaticDestinationCatalog catalog;

    public DestinationCatalogService(StaticDestinationCatalog catalog) {
        this.catalog = catalog;
    }

    public List<DestinationCity> getCities(String countryCode) {
        return catalog.findByCountry(countryCode);
    }

    public Optional<DestinationCity> findCity(String countryCode, String cityName) {
        if (cityName == null || cityName.isBlank()) {
            return primaryCity(countryCode);
        }
        String normalized = normalize(cityName);
        return getCities(countryCode).stream()
                .filter(city -> normalize(city.name()).equals(normalized))
                .findFirst();
    }

    public Optional<DestinationCity> primaryCity(String countryCode) {
        return getCities(countryCode).stream().findFirst();
    }

    public DestinationCity primaryCityOrCountryFallback(Country country) {
        return primaryCity(country.getIsoCode()).orElseGet(() -> {
            if (!country.hasCoordinates()) {
                throw new ResourceNotFoundException(
                        "Coordenadas de destino indisponíveis para " + country.getIsoCode());
            }
            return new DestinationCity(
                    country.getIsoCode(),
                    country.getCapitals().isEmpty() ? country.getDisplayName() : country.getCapitals().get(0),
                    country.getLatitude(),
                    country.getLongitude(),
                    List.of(),
                    true);
        });
    }

    private String normalize(String value) {
        String decomposed = Normalizer.normalize(value.trim(), Normalizer.Form.NFD);
        return DIACRITICS.matcher(decomposed)
                .replaceAll("")
                .toLowerCase(Locale.ROOT);
    }
}
