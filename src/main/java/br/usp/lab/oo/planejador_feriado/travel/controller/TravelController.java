package br.usp.lab.oo.planejador_feriado.travel.controller;

import br.usp.lab.oo.planejador_feriado.travel.model.TravelOverview;
import br.usp.lab.oo.planejador_feriado.travel.service.TravelService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/travel")
public class TravelController {

  private final TravelService travelService;

  public TravelController(TravelService travelService) {
    this.travelService = travelService;
  }

  @GetMapping("/{countryCode}")
  public TravelOverview getOverview(@PathVariable String countryCode) {
    if (countryCode == null || countryCode.trim().isEmpty()) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Country code is required"
      );
    }
    return travelService.getOverviewByCountryCode(countryCode);
  }
}
