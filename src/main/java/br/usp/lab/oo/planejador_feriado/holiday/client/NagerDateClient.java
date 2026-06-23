package br.usp.lab.oo.planejador_feriado.holiday.client;

import br.usp.lab.oo.planejador_feriado.common.config.ExternalApisProperties;
import br.usp.lab.oo.planejador_feriado.holiday.dto.HolidayDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class NagerDateClient {

    private final RestClient restClient;

    public NagerDateClient(ExternalApisProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.nagerDate().baseUrl())
                .build();
    }

    public List<HolidayDTO> getPublicHolidays(int year, String countryCode) {
        return restClient.get()
                .uri("/PublicHolidays/{year}/{countryCode}", year, countryCode)
                .retrieve()
                .body(new ParameterizedTypeReference<List<HolidayDTO>>() {});
    }
}