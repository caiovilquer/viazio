package br.usp.lab.oo.planejador_feriado.holiday.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import br.usp.lab.oo.planejador_feriado.common.exception.ExternalApiException;
import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.holiday.client.NagerDateClient;
import br.usp.lab.oo.planejador_feriado.holiday.dto.HolidayDTO;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class HolidayServiceTest {

  @Mock
  private NagerDateClient client;

  @InjectMocks
  private HolidayService service;

  @Test
  void getHolidaysForYearShouldMapAndSortByDate() {
    HolidayDTO later = new HolidayDTO(
      LocalDate.of(2026, 12, 25),
      "Christmas Day",
      "Natal",
      List.of("Public")
    );
    HolidayDTO earlier = new HolidayDTO(
      LocalDate.of(2026, 1, 1),
      "New Year's Day",
      "Confraternização",
      List.of("Public")
    );

    when(client.getPublicHolidays(2026, "BR")).thenReturn(
      List.of(later, earlier)
    );

    List<Holiday> holidays = service.getHolidaysForYear("BR", 2026);

    assertEquals(2, holidays.size());
    assertEquals(LocalDate.of(2026, 1, 1), holidays.get(0).getDate());
    assertEquals(LocalDate.of(2026, 12, 25), holidays.get(1).getDate());
    assertEquals("Christmas Day", holidays.get(1).getName());
  }

  @Test
  void getHolidaysForYearShouldReturnEmptyForNull() {
    when(client.getPublicHolidays(2026, "BR")).thenReturn(null);

    assertTrue(service.getHolidaysForYear("BR", 2026).isEmpty());
  }

  @Test
  void getHolidaysForYearShouldReturnEmptyForEmpty() {
    when(client.getPublicHolidays(2026, "BR")).thenReturn(List.of());

    assertTrue(service.getHolidaysForYear("BR", 2026).isEmpty());
  }

  @Test
  void getHolidaysForYearShouldReturnEmptyWhenUpstreamReturns404() {
    when(client.getPublicHolidays(2026, "ZZ")).thenThrow(
      HttpClientErrorException.create(
        HttpStatus.NOT_FOUND,
        "Not Found",
        HttpHeaders.EMPTY,
        new byte[0],
        null
      )
    );

    assertTrue(service.getHolidaysForYear("ZZ", 2026).isEmpty());
  }

  @Test
  void getHolidaysForYearShouldThrowExternalApiWhenClientFails() {
    when(client.getPublicHolidays(2026, "BR")).thenThrow(
      new RestClientException("offline")
    );

    assertThrows(ExternalApiException.class, () ->
      service.getHolidaysForYear("BR", 2026)
    );
  }

  @Test
  void getHolidaysForYearByCountryShouldDelegateToIsoCode() {
    Country brazil = new Country(
      "Brazil",
      "BR",
      "Americas",
      "South America",
      List.of("Brasília"),
      List.of("Portuguese"),
      List.of("BRL"),
      List.of("UTC-03:00")
    );
    HolidayDTO dto = new HolidayDTO(
      LocalDate.of(2026, 4, 21),
      "Tiradentes",
      "Tiradentes",
      List.of("Public")
    );

    when(client.getPublicHolidays(2026, "BR")).thenReturn(List.of(dto));

    List<Holiday> holidays = service.getHolidaysForYear(brazil, 2026);

    assertEquals(1, holidays.size());
    assertEquals("Tiradentes", holidays.get(0).getName());
  }

  @Test
  void getHolidaysInWindowShouldFilterByRangeAcrossYears() {
    HolidayDTO endOf2026 = new HolidayDTO(
      LocalDate.of(2026, 12, 25),
      "Christmas",
      "Natal",
      List.of("Public")
    );
    HolidayDTO before = new HolidayDTO(
      LocalDate.of(2026, 1, 1),
      "New Year",
      "Ano Novo",
      List.of("Public")
    );
    HolidayDTO startOf2027 = new HolidayDTO(
      LocalDate.of(2027, 1, 1),
      "New Year",
      "Ano Novo",
      List.of("Public")
    );
    HolidayDTO after = new HolidayDTO(
      LocalDate.of(2027, 4, 21),
      "Tiradentes",
      "Tiradentes",
      List.of("Public")
    );

    when(client.getPublicHolidays(2026, "BR")).thenReturn(
      List.of(before, endOf2026)
    );
    when(client.getPublicHolidays(2027, "BR")).thenReturn(
      List.of(startOf2027, after)
    );

    List<Holiday> holidays = service.getHolidaysInWindow(
      "BR",
      LocalDate.of(2026, 12, 1),
      LocalDate.of(2027, 1, 31)
    );

    assertEquals(2, holidays.size());
    assertEquals(LocalDate.of(2026, 12, 25), holidays.get(0).getDate());
    assertEquals(LocalDate.of(2027, 1, 1), holidays.get(1).getDate());
  }

  @Test
  void getHolidaysInWindowShouldOnlyIncludeMatchingSubdivision() {
    HolidayDTO national = new HolidayDTO(
      LocalDate.of(2026, 7, 9),
      "National",
      "Nacional",
      List.of("Public"),
      true,
      List.of()
    );
    HolidayDTO saoPaulo = new HolidayDTO(
      LocalDate.of(2026, 7, 9),
      "São Paulo",
      "São Paulo",
      List.of("Public"),
      false,
      List.of("BR-SP")
    );
    HolidayDTO rio = new HolidayDTO(
      LocalDate.of(2026, 7, 9),
      "Rio",
      "Rio",
      List.of("Public"),
      false,
      List.of("BR-RJ")
    );
    when(client.getPublicHolidays(2026, "BR")).thenReturn(
      List.of(national, saoPaulo, rio)
    );

    List<Holiday> withoutSubdivision = service.getHolidaysInWindow(
      "BR",
      LocalDate.of(2026, 7, 1),
      LocalDate.of(2026, 7, 31)
    );
    List<Holiday> forSaoPaulo = service.getHolidaysInWindow(
      "BR",
      "BR-SP",
      LocalDate.of(2026, 7, 1),
      LocalDate.of(2026, 7, 31)
    );

    assertEquals(
      List.of("National"),
      withoutSubdivision.stream().map(Holiday::getName).toList()
    );
    assertEquals(
      List.of("National", "São Paulo"),
      forSaoPaulo.stream().map(Holiday::getName).toList()
    );
  }

  @Test
  void getUpcomingHolidaysShouldDropPastDatesInCurrentYear() {
    Country brazil = new Country(
      "Brazil",
      "BR",
      "Americas",
      "South America",
      List.of("Brasília"),
      List.of("Portuguese"),
      List.of("BRL"),
      List.of("UTC-03:00")
    );
    int currentYear = LocalDate.now().getYear();
    LocalDate past = LocalDate.of(currentYear, 1, 1);
    LocalDate future = LocalDate.of(currentYear, 12, 31);

    HolidayDTO pastDto = new HolidayDTO(
      past,
      "Past Holiday",
      "Passado",
      List.of("Public")
    );
    HolidayDTO futureDto = new HolidayDTO(
      future,
      "Future Holiday",
      "Futuro",
      List.of("Public")
    );

    when(client.getPublicHolidays(eq(currentYear), eq("BR"))).thenReturn(
      List.of(pastDto, futureDto)
    );

    List<Holiday> upcoming = service.getUpcomingHolidays(brazil);

    assertTrue(
      upcoming.stream().allMatch(h -> !h.getDate().isBefore(LocalDate.now()))
    );
    assertTrue(upcoming.stream().anyMatch(h -> h.getDate().equals(future)));
    assertTrue(upcoming.stream().noneMatch(h -> h.getDate().equals(past)));
  }
}
