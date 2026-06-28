package br.usp.lab.oo.planejador_feriado.destination.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.usp.lab.oo.planejador_feriado.destination.model.DestinationCity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class StaticDestinationCatalogTest {

  private final StaticDestinationCatalog catalog = new StaticDestinationCatalog(
    new ObjectMapper()
  );

  @Test
  void loadsPrimaryBrazilianCapitalWithCoordinatesAndTimeZone() {
    List<DestinationCity> cities = catalog.findByCountry("br");

    assertFalse(cities.isEmpty());
    DestinationCity brasilia = cities.get(0);
    assertEquals("BR", brasilia.countryCode());
    assertEquals("Brasília", brasilia.name());
    assertTrue(brasilia.primary());
    assertEquals(-15.7934, brasilia.latitude(), 0.01);
    assertEquals(-47.8823, brasilia.longitude(), 0.01);
    assertTrue(brasilia.utcOffsets().contains(-3.0));
  }

  @Test
  void keepsCountriesWithMultipleCapitalCities() {
    List<DestinationCity> cities = catalog.findByCountry("ZA");

    assertTrue(cities.size() >= 3);
    assertTrue(
      cities.stream().anyMatch(city -> city.name().equals("Pretoria"))
    );
    assertTrue(
      cities.stream().anyMatch(city -> city.name().equals("Cape Town"))
    );
  }

  @Test
  void returnsEmptyListForUnknownCountry() {
    assertTrue(catalog.findByCountry("ZZ").isEmpty());
  }
}
