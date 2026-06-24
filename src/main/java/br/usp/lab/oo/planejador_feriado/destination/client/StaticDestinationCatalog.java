package br.usp.lab.oo.planejador_feriado.destination.client;

import br.usp.lab.oo.planejador_feriado.destination.model.DestinationCity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Catálogo local de capitais e coordenadas derivado do Wikidata (CC0). Evita
 * geocodificar dezenas de destinos a cada ranking e torna o resultado determinístico.
 */
@Component
public class StaticDestinationCatalog {

    private final Map<String, List<DestinationCity>> citiesByCountry;

    public StaticDestinationCatalog(ObjectMapper objectMapper) {
        CatalogFile catalog = load(objectMapper);
        this.citiesByCountry = catalog.cities().stream()
                .collect(Collectors.groupingBy(
                        city -> city.countryCode().toUpperCase(Locale.ROOT),
                        Collectors.collectingAndThen(Collectors.toList(), List::copyOf)));
    }

    public List<DestinationCity> findByCountry(String countryCode) {
        return citiesByCountry.getOrDefault(countryCode.toUpperCase(Locale.ROOT), List.of()).stream()
                .sorted(Comparator.comparing(DestinationCity::primary).reversed()
                        .thenComparing(DestinationCity::name))
                .toList();
    }

    private CatalogFile load(ObjectMapper objectMapper) {
        try (InputStream input = new ClassPathResource("data/destinations.json").getInputStream()) {
            return objectMapper.readValue(input, CatalogFile.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao carregar catálogo de destinos", e);
        }
    }

    private record CatalogFile(String source, String retrievedAt, List<DestinationCity> cities) {
        private CatalogFile {
            cities = cities != null ? List.copyOf(cities) : List.of();
        }
    }
}
