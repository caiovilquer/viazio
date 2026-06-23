package br.usp.lab.oo.planejador_feriado.holiday.client;

import br.usp.lab.oo.planejador_feriado.holiday.dto.HolidayDTO;

import java.util.List;

/**
 * Abstrai o acesso a feriados públicos, permitindo decorar a implementação real
 * (ex.: com cache) sem que {@code HolidayService} precise conhecer o detalhe.
 */
public interface HolidayClient {

    List<HolidayDTO> getPublicHolidays(int year, String countryCode);
}
