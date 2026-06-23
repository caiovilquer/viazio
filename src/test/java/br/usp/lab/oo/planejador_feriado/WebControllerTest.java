package br.usp.lab.oo.planejador_feriado;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.recommendation.dto.RecommendationResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoredCriterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TravelRecommendation;
import br.usp.lab.oo.planejador_feriado.recommendation.service.TravelRecommendationEngine;
import br.usp.lab.oo.planejador_feriado.travel.model.TravelOverview;
import br.usp.lab.oo.planejador_feriado.travel.service.TravelService;
import br.usp.lab.oo.planejador_feriado.web.WebController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebController.class)
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TravelService travelService;

    @MockitoBean
    private TravelRecommendationEngine recommendationEngine;

    private TravelOverview overviewFor(String name, String isoCode) {
        Country country = new Country(name, isoCode, "Americas", "South America",
                List.of("Brasília"), List.of("Portuguese"), List.of("BRL"), List.of("UTC-03:00"));
        return new TravelOverview(country, List.of(), null);
    }

    @Test
    void paginaInicialDeveRetornarTemplateIndex() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void buscaComCodigoValidoDeveRetornarTemplateResultado() throws Exception {
        when(travelService.getOverviewByQuery("BR")).thenReturn(overviewFor("Brazil", "BR"));

        mockMvc.perform(get("/viagem").param("destino", "BR"))
                .andExpect(status().isOk())
                .andExpect(view().name("resultado"))
                .andExpect(model().attributeExists("overview"))
                .andExpect(model().attributeDoesNotExist("erro"));
    }

    @Test
    void buscaComCodigoViaParametroLegadoDeveRetornarTemplateResultado() throws Exception {
        when(travelService.getOverviewByQuery("BR")).thenReturn(overviewFor("Brazil", "BR"));

        mockMvc.perform(get("/viagem").param("codigo", "BR"))
                .andExpect(status().isOk())
                .andExpect(view().name("resultado"))
                .andExpect(model().attributeExists("overview"))
                .andExpect(model().attributeDoesNotExist("erro"));
    }

    @Test
    void buscaComNomeValidoDeveRetornarTemplateResultado() throws Exception {
        when(travelService.getOverviewByQuery("brazil")).thenReturn(overviewFor("Brazil", "BR"));

        mockMvc.perform(get("/viagem").param("destino", "brazil"))
                .andExpect(status().isOk())
                .andExpect(view().name("resultado"))
                .andExpect(model().attributeExists("overview"))
                .andExpect(model().attributeDoesNotExist("erro"));
    }

    @Test
    void buscaComCodigoInvalidoDeveRetornarErroAmigavel() throws Exception {
        when(travelService.getOverviewByQuery("ZZ")).thenThrow(new RuntimeException("Country not found"));

        mockMvc.perform(get("/viagem").param("destino", "ZZ"))
                .andExpect(status().isOk())
                .andExpect(view().name("resultado"))
                .andExpect(model().attributeExists("erro"))
                .andExpect(model().attributeDoesNotExist("overview"));
    }

    @Test
    void buscaComNomeInvalidoDeveRetornarErroAmigavel() throws Exception {
        when(travelService.getOverviewByQuery("pais-inexistente-xyz"))
                .thenThrow(new RuntimeException("Country not found"));

        mockMvc.perform(get("/viagem").param("destino", "pais-inexistente-xyz"))
                .andExpect(status().isOk())
                .andExpect(view().name("resultado"))
                .andExpect(model().attributeExists("erro"))
                .andExpect(model().attributeDoesNotExist("overview"));
    }

    @Test
    void buscaComDestinoVazioDeveRetornarMensagemDeErro() throws Exception {
        mockMvc.perform(get("/viagem").param("destino", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("resultado"))
                .andExpect(model().attributeExists("erro"));
    }

    @Test
    void buscaComCodigoDeveNormalizarTermoBuscado() throws Exception {
        when(travelService.getOverviewByQuery("jp")).thenReturn(overviewFor("Japan", "JP"));

        mockMvc.perform(get("/viagem").param("destino", "jp"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("termoBuscado", "Japan"));
    }

    @Test
    void recomendacoesSemParamsDeveRedirecionarParaHome() throws Exception {
        mockMvc.perform(get("/recomendacoes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void recomendacoesComCountriesValidoDeveRetornarResultado() throws Exception {
        when(recommendationEngine.recommend(any())).thenReturn(mockRecommendationResponse());

        mockMvc.perform(get("/recomendacoes")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .param("countries", "JP,FR"))
                .andExpect(status().isOk())
                .andExpect(view().name("recomendacoes-resultado"))
                .andExpect(model().attributeExists("resultado"))
                .andExpect(model().attributeDoesNotExist("erro"));
    }

    @Test
    void recomendacoesComRegionValidoDeveRetornarResultado() throws Exception {
        when(recommendationEngine.recommend(any())).thenReturn(mockRecommendationResponse());

        mockMvc.perform(get("/recomendacoes")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .param("region", "Europe"))
                .andExpect(status().isOk())
                .andExpect(view().name("recomendacoes-resultado"))
                .andExpect(model().attributeExists("resultado"))
                .andExpect(model().attributeDoesNotExist("erro"));
    }

    @Test
    void recomendacoesComFromMaiorQueToDeveRetornarErroAmigavel() throws Exception {
        mockMvc.perform(get("/recomendacoes")
                        .param("from", "2026-06-30")
                        .param("to", "2026-06-01")
                        .param("countries", "JP"))
                .andExpect(status().isOk())
                .andExpect(view().name("recomendacoes-resultado"))
                .andExpect(model().attributeExists("erro"))
                .andExpect(model().attributeDoesNotExist("resultado"));
    }

    @Test
    void recomendacoesComDatasInvalidasDeveRetornarErroAmigavel() throws Exception {
        mockMvc.perform(get("/recomendacoes")
                        .param("from", "data-invalida")
                        .param("to", "2026-06-01")
                        .param("countries", "JP"))
                .andExpect(status().isOk())
                .andExpect(view().name("recomendacoes-resultado"))
                .andExpect(model().attributeExists("erro"))
                .andExpect(model().attributeDoesNotExist("resultado"));
    }

    @Test
    void recomendacoesComCountriesERegionDeveRetornarErroAmigavel() throws Exception {
        mockMvc.perform(get("/recomendacoes")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .param("countries", "JP")
                        .param("region", "Europe"))
                .andExpect(status().isOk())
                .andExpect(view().name("recomendacoes-resultado"))
                .andExpect(model().attributeExists("erro"))
                .andExpect(model().attributeDoesNotExist("resultado"));
    }

    @Test
    void recomendacoesSemCandidatoDeveRetornarErroAmigavel() throws Exception {
        mockMvc.perform(get("/recomendacoes")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(view().name("recomendacoes-resultado"))
                .andExpect(model().attributeExists("erro"))
                .andExpect(model().attributeDoesNotExist("resultado"));
    }

    @Test
    void recomendacoesComLimiteInvalidoDeveRetornarErroAmigavel() throws Exception {
        mockMvc.perform(get("/recomendacoes")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .param("countries", "JP")
                        .param("limit", "99"))
                .andExpect(status().isOk())
                .andExpect(view().name("recomendacoes-resultado"))
                .andExpect(model().attributeExists("erro"))
                .andExpect(model().attributeDoesNotExist("resultado"));
    }

    @Test
    void recomendacoesComEngineFalhandoDeveRetornarErroAmigavel() throws Exception {
        when(recommendationEngine.recommend(any())).thenThrow(new RuntimeException("offline"));

        mockMvc.perform(get("/recomendacoes")
                        .param("from", "2026-06-01")
                        .param("to", "2026-06-30")
                        .param("countries", "JP"))
                .andExpect(status().isOk())
                .andExpect(view().name("recomendacoes-resultado"))
                .andExpect(model().attributeExists("erro"))
                .andExpect(model().attributeDoesNotExist("resultado"));
    }

    private RecommendationResponse mockRecommendationResponse() {
        return new RecommendationResponse(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                "padrão",
                java.util.Map.of("exchange", 0.15, "weather", 0.20),
                List.of(),
                List.of(new TravelRecommendation(
                        "JP",
                        "Japan",
                        68.0,
                        List.of(new ScoredCriterion("exchange", "Câmbio", "💱", true, 100.0, 0.15, 15.0, "ok")),
                        List.of("câmbio favorável"),
                        "JP — score 68: câmbio favorável"
                )),
                List.of()
        );
    }
}
