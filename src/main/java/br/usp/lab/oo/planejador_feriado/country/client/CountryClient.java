package br.usp.lab.oo.planejador_feriado.country.client;

import br.usp.lab.oo.planejador_feriado.country.dto.CountryDTO;
import java.util.List;

/**
 * Abstrai o acesso a dados de países, permitindo decorar a implementação real
 * (ex.: com cache) sem que {@code CountryService} precise conhecer o detalhe.
 */
public interface CountryClient {
  List<CountryDTO> getAllCountries();

  List<CountryDTO> getCountryByCode(String countryCode);

  List<CountryDTO> getCountryByName(String countryName);

  List<CountryDTO> getCountriesByRegion(String region);
}
