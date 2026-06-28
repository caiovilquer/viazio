package br.usp.lab.oo.planejador_feriado.enrichment.model;

/**
 * Perfil descritivo de um destino — população e resumo/imagem da Wikipédia — usado
 * apenas para enriquecer a apresentação (não participa do score). Campos podem ser
 * {@code null} quando a fonte correspondente não respondeu.
 */
public record DestinationProfile(
  String flagEmoji,
  Long population,
  String populationYear,
  String description,
  String extract,
  String imageUrl,
  String wikipediaUrl
) {}
