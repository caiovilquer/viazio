package br.usp.lab.oo.planejador_feriado.cost.service;

import br.usp.lab.oo.planejador_feriado.common.worldbank.WorldBankIndicatorClient;
import br.usp.lab.oo.planejador_feriado.common.worldbank.WorldBankIndicatorPoint;
import br.usp.lab.oo.planejador_feriado.cost.model.CostOfLiving;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CostOfLivingServiceTest {

    @Mock
    private WorldBankIndicatorClient client;

    @Test
    void computesPriceLevelFromLatestCommonYear() {
        CostOfLivingService service = new CostOfLivingService(client);
        when(client.getIndicatorSeries(eq("AR"), eq("PA.NUS.PRVT.PP"))).thenReturn(List.of(
                new WorldBankIndicatorPoint("2021", 43.0),
                new WorldBankIndicatorPoint("2020", 30.0)));
        when(client.getIndicatorSeries(eq("AR"), eq("PA.NUS.FCRF"))).thenReturn(List.of(
                new WorldBankIndicatorPoint("2024", 900.0),
                new WorldBankIndicatorPoint("2021", 95.0)));

        Optional<CostOfLiving> result = service.getPriceLevel("AR");

        assertTrue(result.isPresent());
        // ano comum mais recente = 2021 → 43 / 95
        assertEquals("2021", result.get().year());
        assertEquals(43.0 / 95.0, result.get().priceLevelRatio(), 0.0001);
    }

    @Test
    void emptyWhenNoCommonYear() {
        CostOfLivingService service = new CostOfLivingService(client);
        when(client.getIndicatorSeries(eq("XX"), eq("PA.NUS.PRVT.PP"))).thenReturn(List.of(
                new WorldBankIndicatorPoint("2021", 43.0)));
        when(client.getIndicatorSeries(eq("XX"), eq("PA.NUS.FCRF"))).thenReturn(List.of(
                new WorldBankIndicatorPoint("2019", 50.0)));

        assertTrue(service.getPriceLevel("XX").isEmpty());
    }

    @Test
    void emptyWhenOnlyCommonYearIsStale() {
        // Reproduz o caso real da Venezuela: PPP "congelado" num ano de antes da
        // redenominação do bolívar, câmbio oficial seguindo atualizado na moeda nova —
        // o único ano em comum é antigo demais para a razão fazer sentido.
        CostOfLivingService service = new CostOfLivingService(client);
        when(client.getIndicatorSeries(eq("VE"), eq("PA.NUS.PRVT.PP"))).thenReturn(List.of(
                new WorldBankIndicatorPoint("2011", 0.0000000000294362664222717)));
        when(client.getIndicatorSeries(eq("VE"), eq("PA.NUS.FCRF"))).thenReturn(List.of(
                new WorldBankIndicatorPoint("2011", 4.29),
                new WorldBankIndicatorPoint("2024", 36.0)));

        assertTrue(service.getPriceLevel("VE").isEmpty());
    }
}
