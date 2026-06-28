package br.usp.lab.oo.planejador_feriado.enrichment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.usp.lab.oo.planejador_feriado.enrichment.client.WikipediaClient;
import br.usp.lab.oo.planejador_feriado.enrichment.dto.WikipediaSummaryDTO;
import br.usp.lab.oo.planejador_feriado.enrichment.model.WikiSummary;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WikipediaServiceTest {

  @Mock
  private WikipediaClient client;

  @Test
  void prefersPortugueseSummaryWhenAvailable() {
    WikipediaService service = new WikipediaService(client);
    WikipediaSummaryDTO dto = new WikipediaSummaryDTO(
      "país na América do Sul",
      "O Brasil é o maior país da América do Sul...",
      new WikipediaSummaryDTO.ImageDTO("https://thumb.example/brasil.jpg"),
      new WikipediaSummaryDTO.ImageDTO("https://original.example/brasil.jpg"),
      new WikipediaSummaryDTO.ContentUrlsDTO(
        new WikipediaSummaryDTO.DesktopDTO(
          "https://pt.wikipedia.org/wiki/Brasil"
        )
      )
    );
    when(client.getSummary("pt", "Brasil")).thenReturn(dto);

    Optional<WikiSummary> result = service.getCountrySummary(
      "Brasil",
      "Brazil"
    );

    assertTrue(result.isPresent());
    assertEquals("país na América do Sul", result.get().description());
    assertEquals(
      "O Brasil é o maior país da América do Sul...",
      result.get().extract()
    );
    assertEquals(
      "https://original.example/brasil.jpg",
      result.get().imageUrl()
    );
    assertEquals(
      "https://pt.wikipedia.org/wiki/Brasil",
      result.get().pageUrl()
    );
    verify(client, never()).getSummary("en", "Brazil");
  }

  @Test
  void fallsBackToEnglishWhenPortugueseNotFound() {
    WikipediaService service = new WikipediaService(client);
    WikipediaSummaryDTO dto = new WikipediaSummaryDTO(
      "country in East Asia",
      "Japan is an island country...",
      null,
      null,
      null
    );
    when(client.getSummary("pt", "Japão")).thenReturn(null);
    when(client.getSummary("en", "Japan")).thenReturn(dto);

    Optional<WikiSummary> result = service.getCountrySummary("Japão", "Japan");

    assertTrue(result.isPresent());
    assertEquals("country in East Asia", result.get().description());
  }

  @Test
  void usesThumbnailWhenOriginalImageIsAbsent() {
    WikipediaService service = new WikipediaService(client);
    WikipediaSummaryDTO dto = new WikipediaSummaryDTO(
      "desc",
      "extract",
      new WikipediaSummaryDTO.ImageDTO("https://thumb.example/x.jpg"),
      null,
      null
    );
    when(client.getSummary("pt", "X")).thenReturn(dto);

    Optional<WikiSummary> result = service.getCountrySummary("X", "X");

    assertEquals("https://thumb.example/x.jpg", result.get().imageUrl());
  }

  @Test
  void emptyWhenNeitherLanguageHasAnArticle() {
    WikipediaService service = new WikipediaService(client);
    when(client.getSummary("pt", "Atlantida")).thenReturn(null);
    when(client.getSummary("en", "Atlantis")).thenReturn(null);

    Optional<WikiSummary> result = service.getCountrySummary(
      "Atlantida",
      "Atlantis"
    );

    assertTrue(result.isEmpty());
  }

  @Test
  void skipsPortugueseLookupWhenNameIsBlank() {
    WikipediaService service = new WikipediaService(client);
    WikipediaSummaryDTO dto = new WikipediaSummaryDTO(
      "desc",
      "extract",
      null,
      null,
      null
    );
    when(client.getSummary("en", "Japan")).thenReturn(dto);

    Optional<WikiSummary> result = service.getCountrySummary(null, "Japan");

    assertTrue(result.isPresent());
    verify(client, never()).getSummary("pt", null);
  }
}
