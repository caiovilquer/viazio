package br.usp.lab.oo.planejador_feriado.exchange.service;

import br.usp.lab.oo.planejador_feriado.exchange.client.AwesomeApiClient;
import br.usp.lab.oo.planejador_feriado.exchange.dto.ExchangeDTO;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ExchangeService {

    private final AwesomeApiClient client;

    public ExchangeService(AwesomeApiClient client) {
        this.client = client;
    }

    public Exchange getExchangeRate(String currencyCode) {
        String targetKey = currencyCode.toUpperCase() + "BRL";
        
        Map<String, ExchangeDTO> response = client.getExchangeRate(currencyCode.toUpperCase());

        if (response == null || !response.containsKey(targetKey)) {
            throw new RuntimeException("Câmbio de moeda não encontrado: " + currencyCode);
        }

        ExchangeDTO dto = response.get(targetKey);
        return toModel(dto);
    }

    private Exchange toModel(ExchangeDTO dto) {
        return new Exchange(dto.currency(), dto.valueInReais());
    }
}