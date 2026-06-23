package br.usp.lab.oo.planejador_feriado.exchange.client;

import br.usp.lab.oo.planejador_feriado.exchange.dto.ExchangeDTO;

import java.util.Map;

/**
 * Abstrai o acesso a cotações de câmbio, permitindo decorar a implementação real
 * (ex.: com cache) sem que {@code ExchangeService} precise conhecer o detalhe.
 */
public interface ExchangeClient {

    Map<String, ExchangeDTO> getExchangeRate(String currencyCode);
}
