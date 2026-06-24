package br.usp.lab.oo.planejador_feriado.country.client;

import br.usp.lab.oo.planejador_feriado.country.dto.CountryDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CachingCountryClientTest {

    @Mock
    private StaticCountryClient delegate;

    @Test
    void cachesRepeatedCallsByCode() {
        List<CountryDTO> brazil = List.of(new CountryDTO(
                new CountryDTO.NameDTO("Brazil"), "BR", "Americas", "South America",
                List.of("Brasília"), Map.of("por", "Portuguese"), Map.of(), List.of("UTC-03:00"),
                List.of(-10.0, -55.0)));
        when(delegate.getCountryByCode("BR")).thenReturn(brazil);

        CachingCountryClient client = new CachingCountryClient(delegate);

        List<CountryDTO> first = client.getCountryByCode("BR");
        List<CountryDTO> second = client.getCountryByCode("br");

        assertSame(brazil, first);
        assertSame(brazil, second);
        verify(delegate, times(1)).getCountryByCode("BR");
    }

    @Test
    void doesNotMixCacheKeysAcrossOperations() {
        when(delegate.getCountryByName("Brazil")).thenReturn(List.of());
        when(delegate.getCountriesByRegion("Americas")).thenReturn(List.of());

        CachingCountryClient client = new CachingCountryClient(delegate);
        client.getCountryByName("Brazil");
        client.getCountriesByRegion("Americas");

        verify(delegate, times(1)).getCountryByName("Brazil");
        verify(delegate, times(1)).getCountriesByRegion("Americas");
    }
}
