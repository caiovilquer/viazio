package br.usp.lab.oo.planejador_feriado.common.worldbank;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CachingWorldBankIndicatorClientTest {

    @Mock
    private WorldBankClient delegate;

    @Test
    void cachesRepeatedCallsByIsoCodeAndIndicator() {
        List<WorldBankIndicatorPoint> series = List.of(new WorldBankIndicatorPoint("2023", 216_000_000.0));
        when(delegate.getIndicatorSeries("BR", "SP.POP.TOTL")).thenReturn(series);

        CachingWorldBankIndicatorClient client = new CachingWorldBankIndicatorClient(delegate);

        List<WorldBankIndicatorPoint> first = client.getIndicatorSeries("BR", "SP.POP.TOTL");
        List<WorldBankIndicatorPoint> second = client.getIndicatorSeries("br", "SP.POP.TOTL");

        assertSame(series, first);
        assertSame(series, second);
        verify(delegate, times(1)).getIndicatorSeries("BR", "SP.POP.TOTL");
    }

    @Test
    void treatsDifferentIndicatorsAsDifferentCacheEntries() {
        List<WorldBankIndicatorPoint> population = List.of(new WorldBankIndicatorPoint("2023", 216_000_000.0));
        List<WorldBankIndicatorPoint> ppp = List.of(new WorldBankIndicatorPoint("2021", 43.0));
        when(delegate.getIndicatorSeries("BR", "SP.POP.TOTL")).thenReturn(population);
        when(delegate.getIndicatorSeries("BR", "PA.NUS.PRVT.PP")).thenReturn(ppp);

        CachingWorldBankIndicatorClient client = new CachingWorldBankIndicatorClient(delegate);

        assertSame(population, client.getIndicatorSeries("BR", "SP.POP.TOTL"));
        assertSame(ppp, client.getIndicatorSeries("BR", "PA.NUS.PRVT.PP"));
        verify(delegate, times(1)).getIndicatorSeries("BR", "SP.POP.TOTL");
        verify(delegate, times(1)).getIndicatorSeries("BR", "PA.NUS.PRVT.PP");
    }
}
