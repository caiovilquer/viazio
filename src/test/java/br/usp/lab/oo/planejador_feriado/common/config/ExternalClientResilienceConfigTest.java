package br.usp.lab.oo.planejador_feriado.common.config;

import br.usp.lab.oo.planejador_feriado.common.worldbank.WorldBankClient;
import br.usp.lab.oo.planejador_feriado.enrichment.client.WikipediaRestClient;
import br.usp.lab.oo.planejador_feriado.exchange.client.AwesomeApiClient;
import br.usp.lab.oo.planejador_feriado.holiday.client.NagerDateClient;
import br.usp.lab.oo.planejador_feriado.weather.client.OpenMeteoClient;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExternalClientResilienceConfigTest {

    @Test
    void isolatesResilienceStateByExternalProvider() throws Exception {
        assertResilienceName(
                NagerDateClient.class.getMethod("getPublicHolidays", int.class, String.class),
                "holidayApi");
        assertResilienceName(
                AwesomeApiClient.class.getMethod("getExchangeRate", String.class),
                "exchangeApi");
        assertResilienceName(
                WorldBankClient.class.getMethod("getIndicatorSeries", String.class, String.class),
                "worldBankApi");
        assertResilienceName(
                WikipediaRestClient.class.getMethod("getSummary", String.class, String.class),
                "wikipediaApi");
        assertResilienceName(
                OpenMeteoClient.class.getMethod(
                        "getHistoricalDaily",
                        double.class,
                        double.class,
                        LocalDate.class,
                        LocalDate.class),
                "weatherApi");
        assertResilienceName(
                OpenMeteoClient.class.getMethod(
                        "getForecastDaily",
                        double.class,
                        double.class,
                        LocalDate.class,
                        LocalDate.class),
                "weatherApi");
    }

    private void assertResilienceName(Method method, String expected) {
        assertEquals(expected, method.getAnnotation(Retry.class).name());
        assertEquals(expected, method.getAnnotation(CircuitBreaker.class).name());
        assertEquals(expected, method.getAnnotation(Bulkhead.class).name());
    }
}
