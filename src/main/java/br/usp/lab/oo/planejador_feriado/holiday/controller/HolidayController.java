package br.usp.lab.oo.planejador_feriado.holiday.controller;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.holiday.service.HolidayService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/holidays")
public class HolidayController {

    private final CountryService countryService;
    private final HolidayService holidayService;

    public HolidayController(CountryService countryService, HolidayService holidayService) {
        this.countryService = countryService;
        this.holidayService = holidayService;
    }

    @GetMapping("/{countryCode}")
    public List<Holiday> getUpcomingHolidays(@PathVariable String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Country code is required");
        }
        Country country = countryService.getCountryByCode(countryCode);
        return holidayService.getUpcomingHolidays(country);
    }
}
