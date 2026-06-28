package br.usp.lab.oo.planejador_feriado.country.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.usp.lab.oo.planejador_feriado.common.exception.ExternalApiException;
import br.usp.lab.oo.planejador_feriado.common.exception.ResourceNotFoundException;
import br.usp.lab.oo.planejador_feriado.country.client.CountryClient;
import br.usp.lab.oo.planejador_feriado.country.dto.CountryDTO;
import br.usp.lab.oo.planejador_feriado.country.model.Country;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class CountryServiceTest {

  @Mock
  private CountryClient client;

  @InjectMocks
  private CountryService service;

  private CountryDTO brazilDto() {
    return new CountryDTO(
      new CountryDTO.NameDTO(
        "Brazil",
        Map.of("por", new CountryDTO.TranslationDTO("Brasil"))
      ),
      "BR",
      "Americas",
      "South America",
      List.of("Brasília"),
      Map.of("por", "Portuguese"),
      Map.of("BRL", new CountryDTO.CurrencyDTO("Brazilian real", "R$")),
      List.of("UTC-03:00"),
      List.of(-10.0, -55.0)
    );
  }

  @Test
  void shouldMapDtoToModelByCode() {
    when(client.getCountryByCode("BR")).thenReturn(List.of(brazilDto()));

    Country country = service.getCountryByCode("BR");

    assertEquals("Brazil", country.getName());
    assertEquals("Brasil", country.getDisplayName());
    assertEquals("BR", country.getIsoCode());
    assertEquals("Americas", country.getRegion());
    assertEquals("South America", country.getSubregion());
    assertEquals(List.of("Brasília"), country.getCapitals());
    assertEquals("Portuguese", country.getMainLanguage());
    assertEquals("BRL", country.getMainCurrency());
    assertEquals(List.of("UTC-03:00"), country.getTimezones());
    assertTrue(country.hasCoordinates());
    assertEquals(-10.0, country.getLatitude());
    assertEquals(-55.0, country.getLongitude());
    verify(client).getCountryByCode("BR");
  }

  @Test
  void shouldMapDtoToModelByName() {
    when(client.getCountryByName("brazil")).thenReturn(List.of(brazilDto()));

    Country country = service.getCountryByName("brazil");

    assertEquals("Brazil", country.getName());
    verify(client).getCountryByName("brazil");
  }

  @Test
  void shouldThrowNotFoundWhenCodeReturnsEmptyList() {
    when(client.getCountryByCode("ZZ")).thenReturn(List.of());

    ResourceNotFoundException ex = assertThrows(
      ResourceNotFoundException.class,
      () -> service.getCountryByCode("ZZ")
    );
    assertEquals("Country not found", ex.getMessage());
  }

  @Test
  void shouldThrowNotFoundWhenCodeReturnsNull() {
    when(client.getCountryByCode("ZZ")).thenReturn(null);

    assertThrows(ResourceNotFoundException.class, () ->
      service.getCountryByCode("ZZ")
    );
  }

  @Test
  void shouldThrowNotFoundWhenUpstreamReturns404() {
    when(client.getCountryByCode("ZZ")).thenThrow(notFound());

    ResourceNotFoundException ex = assertThrows(
      ResourceNotFoundException.class,
      () -> service.getCountryByCode("ZZ")
    );
    assertTrue(ex.getMessage().contains("ZZ"));
  }

  @Test
  void shouldThrowExternalApiWhenClientFailsForCode() {
    when(client.getCountryByCode("BR")).thenThrow(
      new RestClientException("offline")
    );

    assertThrows(ExternalApiException.class, () ->
      service.getCountryByCode("BR")
    );
  }

  @Test
  void shouldThrowNotFoundWhenNameReturnsEmptyList() {
    when(client.getCountryByName("nowhere")).thenReturn(List.of());

    assertThrows(ResourceNotFoundException.class, () ->
      service.getCountryByName("nowhere")
    );
  }

  @Test
  void getCountryByQueryShouldUseCodeLookupForTwoLetterInput() {
    when(client.getCountryByCode("BR")).thenReturn(List.of(brazilDto()));

    Country country = service.getCountryByQuery("BR");

    assertEquals("Brazil", country.getName());
    verify(client).getCountryByCode("BR");
  }

  @Test
  void getCountryByQueryShouldUseNameLookupForLongerInput() {
    when(client.getCountryByName("brazil")).thenReturn(List.of(brazilDto()));

    Country country = service.getCountryByQuery("brazil");

    assertEquals("Brazil", country.getName());
    verify(client).getCountryByName("brazil");
  }

  @Test
  void looksLikeIsoCodeShouldDetectTwoLetterCodes() {
    assertTrue(CountryService.looksLikeIsoCode("BR"));
    assertTrue(CountryService.looksLikeIsoCode("jp"));
    assertFalse(CountryService.looksLikeIsoCode("brazil"));
    assertFalse(CountryService.looksLikeIsoCode("B1"));
    assertFalse(CountryService.looksLikeIsoCode("B"));
  }

  @Test
  void getCountriesByRegionShouldRespectLimit() {
    CountryDTO france = new CountryDTO(
      new CountryDTO.NameDTO("France", null),
      "FR",
      "Europe",
      "Western Europe",
      List.of("Paris"),
      Map.of("fra", "French"),
      Map.of("EUR", new CountryDTO.CurrencyDTO("Euro", "€")),
      List.of("UTC+01:00"),
      List.of(46.0, 2.0)
    );
    CountryDTO italy = new CountryDTO(
      new CountryDTO.NameDTO("Italy", null),
      "IT",
      "Europe",
      "Southern Europe",
      List.of("Rome"),
      Map.of("ita", "Italian"),
      Map.of("EUR", new CountryDTO.CurrencyDTO("Euro", "€")),
      List.of("UTC+01:00"),
      List.of(42.8, 12.8)
    );
    CountryDTO spain = new CountryDTO(
      new CountryDTO.NameDTO("Spain", null),
      "ES",
      "Europe",
      "Southern Europe",
      List.of("Madrid"),
      Map.of("spa", "Spanish"),
      Map.of("EUR", new CountryDTO.CurrencyDTO("Euro", "€")),
      List.of("UTC+01:00"),
      List.of(40.0, -4.0)
    );

    when(client.getCountriesByRegion("Europe")).thenReturn(
      List.of(france, italy, spain)
    );

    List<Country> result = service.getCountriesByRegion("Europe", 2);

    assertEquals(2, result.size());
    assertEquals("France", result.get(0).getName());
    assertEquals("Italy", result.get(1).getName());
  }

  @Test
  void getCountriesByRegionShouldExcludeNonIndependentTerritories() {
    CountryDTO france = new CountryDTO(
      new CountryDTO.NameDTO("France", null),
      "FR",
      "Europe",
      "Western Europe",
      List.of("Paris"),
      Map.of("fra", "French"),
      Map.of("EUR", new CountryDTO.CurrencyDTO("Euro", "€")),
      List.of("UTC+01:00"),
      List.of(46.0, 2.0),
      true,
      true,
      "officially-assigned"
    );
    CountryDTO aland = new CountryDTO(
      new CountryDTO.NameDTO("Åland Islands", null),
      "AX",
      "Europe",
      "Northern Europe",
      List.of("Mariehamn"),
      Map.of("swe", "Swedish"),
      Map.of("EUR", new CountryDTO.CurrencyDTO("Euro", "€")),
      List.of("UTC+02:00"),
      List.of(60.1, 19.9),
      false,
      false,
      "officially-assigned"
    );
    when(client.getCountriesByRegion("Europe")).thenReturn(
      List.of(aland, france)
    );

    List<Country> result = service.getCountriesByRegion("Europe");

    assertEquals(
      List.of("FR"),
      result.stream().map(Country::getIsoCode).toList()
    );
  }

  @Test
  void getCountriesByRegionShouldThrowNotFoundWhenEmpty() {
    when(client.getCountriesByRegion("Atlantis")).thenReturn(List.of());

    ResourceNotFoundException ex = assertThrows(
      ResourceNotFoundException.class,
      () -> service.getCountriesByRegion("Atlantis", 10)
    );
    assertEquals("Region not found", ex.getMessage());
  }

  @Test
  void getCountriesByRegionShouldThrowExternalApiWhenClientFails() {
    when(client.getCountriesByRegion("Europe")).thenThrow(
      new RestClientException("offline")
    );

    assertThrows(ExternalApiException.class, () ->
      service.getCountriesByRegion("Europe", 10)
    );
  }

  private HttpClientErrorException notFound() {
    return HttpClientErrorException.create(
      HttpStatus.NOT_FOUND,
      "Not Found",
      HttpHeaders.EMPTY,
      new byte[0],
      null
    );
  }
}
