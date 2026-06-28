package br.usp.lab.oo.planejador_feriado.exchange.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true) //ignorar Json n usados
public record ExchangeDTO(
  @JsonProperty("code") String currency,
  @JsonProperty("bid") double valueInReais
) {}
