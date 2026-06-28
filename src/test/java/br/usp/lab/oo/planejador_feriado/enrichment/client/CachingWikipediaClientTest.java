package br.usp.lab.oo.planejador_feriado.enrichment.client;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.usp.lab.oo.planejador_feriado.enrichment.dto.WikipediaSummaryDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CachingWikipediaClientTest {

  @Mock
  private WikipediaRestClient delegate;

  @Test
  void cachesRepeatedCallsByLanguageAndTitle() {
    WikipediaSummaryDTO dto = new WikipediaSummaryDTO(
      "desc",
      "extract",
      null,
      null,
      null
    );
    when(delegate.getSummary("pt", "Brasil")).thenReturn(dto);

    CachingWikipediaClient client = new CachingWikipediaClient(delegate);

    WikipediaSummaryDTO first = client.getSummary("pt", "Brasil");
    WikipediaSummaryDTO second = client.getSummary("PT", "Brasil");

    assertSame(dto, first);
    assertSame(dto, second);
    verify(delegate, times(1)).getSummary("pt", "Brasil");
  }

  @Test
  void cachesNegativeResultsToAvoidRepeatedNotFoundLookups() {
    when(delegate.getSummary("pt", "Atlantida")).thenReturn(null);

    CachingWikipediaClient client = new CachingWikipediaClient(delegate);

    assertNull(client.getSummary("pt", "Atlantida"));
    assertNull(client.getSummary("pt", "Atlantida"));
    verify(delegate, times(1)).getSummary("pt", "Atlantida");
  }
}
