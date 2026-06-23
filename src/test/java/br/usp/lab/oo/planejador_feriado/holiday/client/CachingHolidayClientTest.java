package br.usp.lab.oo.planejador_feriado.holiday.client;

import br.usp.lab.oo.planejador_feriado.holiday.dto.HolidayDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CachingHolidayClientTest {

    @Mock
    private NagerDateClient delegate;

    @Test
    void cachesRepeatedCallsByYearAndCountry() {
        List<HolidayDTO> holidays = List.of(new HolidayDTO(LocalDate.of(2026, 1, 1), "New Year", "Ano Novo", List.of("Public")));
        when(delegate.getPublicHolidays(2026, "BR")).thenReturn(holidays);

        CachingHolidayClient client = new CachingHolidayClient(delegate);

        List<HolidayDTO> first = client.getPublicHolidays(2026, "BR");
        List<HolidayDTO> second = client.getPublicHolidays(2026, "br");

        assertSame(holidays, first);
        assertSame(holidays, second);
        verify(delegate, times(1)).getPublicHolidays(2026, "BR");
    }

    @Test
    void doesNotMixDifferentYears() {
        when(delegate.getPublicHolidays(2026, "BR")).thenReturn(List.of());
        when(delegate.getPublicHolidays(2027, "BR")).thenReturn(List.of());

        CachingHolidayClient client = new CachingHolidayClient(delegate);
        client.getPublicHolidays(2026, "BR");
        client.getPublicHolidays(2027, "BR");

        verify(delegate, times(1)).getPublicHolidays(2026, "BR");
        verify(delegate, times(1)).getPublicHolidays(2027, "BR");
    }
}
