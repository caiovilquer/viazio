package br.usp.lab.oo.planejador_feriado.demographics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import br.usp.lab.oo.planejador_feriado.common.worldbank.WorldBankIndicatorClient;
import br.usp.lab.oo.planejador_feriado.common.worldbank.WorldBankIndicatorPoint;
import br.usp.lab.oo.planejador_feriado.demographics.model.Demographics;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemographicsServiceTest {

  @Mock
  private WorldBankIndicatorClient client;

  @Test
  void resolvesPopulationFromLatestYear() {
    DemographicsService service = new DemographicsService(client);
    when(client.getIndicatorSeries(eq("BR"), eq("SP.POP.TOTL"))).thenReturn(
      List.of(
        new WorldBankIndicatorPoint("2022", 215_000_000.0),
        new WorldBankIndicatorPoint("2023", 216_000_000.0),
        new WorldBankIndicatorPoint("2021", 214_000_000.0)
      )
    );

    Optional<Demographics> result = service.getPopulation("BR");

    assertTrue(result.isPresent());
    assertEquals("2023", result.get().year());
    assertEquals(216_000_000L, result.get().population());
    assertEquals("BR", result.get().isoCode());
  }

  @Test
  void ignoresPointsWithMissingYearOrValue() {
    DemographicsService service = new DemographicsService(client);
    when(client.getIndicatorSeries(eq("BR"), eq("SP.POP.TOTL"))).thenReturn(
      List.of(
        new WorldBankIndicatorPoint("2023", null),
        new WorldBankIndicatorPoint(null, 1.0),
        new WorldBankIndicatorPoint("2021", 214_000_000.0)
      )
    );

    Optional<Demographics> result = service.getPopulation("BR");

    assertTrue(result.isPresent());
    assertEquals("2021", result.get().year());
  }

  @Test
  void emptyWhenNoUsablePoints() {
    DemographicsService service = new DemographicsService(client);
    when(client.getIndicatorSeries(eq("XX"), eq("SP.POP.TOTL"))).thenReturn(
      List.of()
    );

    assertTrue(service.getPopulation("XX").isEmpty());
  }
}
