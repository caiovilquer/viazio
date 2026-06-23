package br.usp.lab.oo.planejador_feriado.cost.client;

import br.usp.lab.oo.planejador_feriado.common.config.ExternalApisProperties;
import br.usp.lab.oo.planejador_feriado.common.config.RestClientFactory;
import br.usp.lab.oo.planejador_feriado.cost.dto.WorldBankIndicatorPoint;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Consome o indicador {@code PA.NUS.PPPC.RF} do Banco Mundial. A API responde um array
 * heterogêneo {@code [ {metadados}, [ {pontos} ] ]}, então o parse é feito manualmente
 * sobre o {@link JsonNode} e convertido para uma lista tipada de pontos.
 */
@Component
public class WorldBankClient implements CostOfLivingClient {

    private static final String PRICE_LEVEL_INDICATOR = "PA.NUS.PPPC.RF";

    private final RestClient restClient;

    public WorldBankClient(RestClientFactory restClientFactory, ExternalApisProperties properties) {
        this.restClient = restClientFactory.builderFor(properties.worldBank().baseUrl()).build();
    }

    @Override
    @Retry(name = "externalApi")
    @CircuitBreaker(name = "externalApi")
    public List<WorldBankIndicatorPoint> getPriceLevelSeries(String isoCode) {
        String code = isoCode.toUpperCase(Locale.ROOT);
        JsonNode root = restClient.get()
                .uri("/country/{code}/indicator/{indicator}?format=json&mrnev=5",
                        code, PRICE_LEVEL_INDICATOR)
                .retrieve()
                .body(JsonNode.class);

        if (root == null || !root.isArray() || root.size() < 2) {
            return List.of();
        }

        JsonNode data = root.get(1);
        if (data == null || !data.isArray()) {
            return List.of();
        }

        List<WorldBankIndicatorPoint> points = new ArrayList<>();
        for (JsonNode point : data) {
            JsonNode valueNode = point.get("value");
            JsonNode dateNode = point.get("date");
            Double value = valueNode != null && !valueNode.isNull() ? valueNode.asDouble() : null;
            String year = dateNode != null && !dateNode.isNull() ? dateNode.asText() : null;
            points.add(new WorldBankIndicatorPoint(year, value));
        }
        return points;
    }
}
