package br.usp.lab.oo.planejador_feriado.holiday.service;

import br.usp.lab.oo.planejador_feriado.common.exception.ExternalApiException;
import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.holiday.client.HolidayClient;
import br.usp.lab.oo.planejador_feriado.holiday.dto.HolidayDTO;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Service
public class HolidayService {

  private final HolidayClient client;

  public HolidayService(HolidayClient client) {
    this.client = client;
  }

  public List<Holiday> getHolidaysForYear(String isoCode, int year) {
    List<HolidayDTO> responseList;
    try {
      responseList = client.getPublicHolidays(year, isoCode);
    } catch (HttpClientErrorException.NotFound e) {
      return List.of();
    } catch (RestClientException e) {
      throw new ExternalApiException(
        "Falha ao consultar serviço de feriados",
        e
      );
    }

    if (responseList == null || responseList.isEmpty()) {
      return List.of();
    }

    List<Holiday> holidays = new ArrayList<>();
    for (HolidayDTO dto : responseList) {
      holidays.add(toModel(dto));
    }

    return holidays
      .stream()
      .sorted(Comparator.comparing(Holiday::getDate))
      .toList();
  }

  public List<Holiday> getHolidaysForYear(Country country, int year) {
    return getHolidaysForYear(country.getIsoCode(), year);
  }

  public List<Holiday> getHolidaysInWindow(
    String isoCode,
    LocalDate from,
    LocalDate to
  ) {
    return getHolidaysInWindow(isoCode, null, from, to);
  }

  public List<Holiday> getHolidaysInWindow(
    String isoCode,
    String subdivisionCode,
    LocalDate from,
    LocalDate to
  ) {
    List<Holiday> holidays = new ArrayList<>();
    for (int year = from.getYear(); year <= to.getYear(); year++) {
      holidays.addAll(getHolidaysForYear(isoCode, year));
    }

    return holidays
      .stream()
      .filter(h -> !h.getDate().isBefore(from) && !h.getDate().isAfter(to))
      .filter(h -> h.appliesTo(subdivisionCode))
      .sorted(Comparator.comparing(Holiday::getDate))
      .toList();
  }

  public List<Holiday> getUpcomingHolidays(Country country) {
    int currentYear = LocalDate.now().getYear();
    LocalDate today = LocalDate.now();

    return getHolidaysForYear(country, currentYear)
      .stream()
      .filter(h -> !h.getDate().isBefore(today))
      .filter(h -> h.appliesTo(null))
      .toList();
  }

  private Holiday toModel(HolidayDTO dto) {
    return new Holiday(
      dto.date(),
      dto.name(),
      dto.localName(),
      dto.types(),
      dto.global(),
      dto.counties()
    );
  }
}
