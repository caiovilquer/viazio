package br.usp.lab.oo.planejador_feriado.holiday.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.holiday.client.NagerDateClient;
import br.usp.lab.oo.planejador_feriado.holiday.dto.HolidayDTO;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import org.springframework.stereotype.Service;

@Service
public class HolidayService {

    private final NagerDateClient client;

    public HolidayService(NagerDateClient client) {
        this.client = client;
    }

    public List<Holiday> getHolidaysForYear(Country country, int year) {
        List<HolidayDTO> responseList =
                client.getPublicHolidays(year, country.getIsoCode());

        if (responseList == null || responseList.isEmpty()) {
            return List.of();
        }

        List<Holiday> holidays = new ArrayList<>();
        for (HolidayDTO dto : responseList) {
            holidays.add(toModel(dto));
        }

        return holidays.stream()
                .sorted(Comparator.comparing(Holiday::getDate))
                .toList();
    }

    public List<Holiday> getUpcomingHolidays(Country country) {
        int currentYear = LocalDate.now().getYear();
        LocalDate today = LocalDate.now();

        return getHolidaysForYear(country, currentYear).stream()
                .filter(h -> !h.getDate().isBefore(today))
                .toList();
    }

    private Holiday toModel(HolidayDTO dto) {
        return new Holiday(
                dto.date(),
                dto.name(),
                dto.localName(),
                dto.types()
        );
    }
}
