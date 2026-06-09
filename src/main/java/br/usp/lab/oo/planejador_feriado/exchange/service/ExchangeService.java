package br.usp.lab.oo.planejador_feriado.exchange.service;

import br.usp.lab.oo.planejador_feriado.common.exception.ExternalApiException;
import br.usp.lab.oo.planejador_feriado.common.exception.ResourceNotFoundException;
import br.usp.lab.oo.planejador_feriado.exchange.client.AwesomeApiClient;
import br.usp.lab.oo.planejador_feriado.exchange.dto.ExchangeDTO;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Service
public class ExchangeService {

    private final AwesomeApiClient client;

    public ExchangeService(AwesomeApiClient client) {
        this.client = client;
    }

    public Exchange getExchangeRate(String currencyCode) {
        String upper = currencyCode.toUpperCase();
        String targetKey = upper + "BRL";

        if ("BRL".equals(upper)) {
            return new Exchange("BRL", 1.00);
        }

        Map<String, ExchangeDTO> response;
        try {
            response = client.getExchangeRate(upper);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Câmbio não encontrado: " + currencyCode);
        } catch (RestClientException e) {
            throw new ExternalApiException("Falha ao consultar serviço de câmbio", e);
        }

        if (response == null || !response.containsKey(targetKey)) {
            throw new ResourceNotFoundException("Câmbio não encontrado: " + currencyCode);
        }

        ExchangeDTO dto = response.get(targetKey);
        return toModel(dto);
    }

    private Exchange toModel(ExchangeDTO dto) {
        return new Exchange(dto.currency(), dto.valueInReais());
    }
}