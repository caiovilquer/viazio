package br.usp.lab.oo.planejador_feriado;

import br.usp.lab.oo.planejador_feriado.recommendation.dto.RecommendationResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TravelRecommendation;
import br.usp.lab.oo.planejador_feriado.recommendation.service.TravelRecommendationEngine;
import br.usp.lab.oo.planejador_feriado.travel.service.TravelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TravelRecommendationEngine recommendationEngine;

    @Test
    void paginaInicialDeveRetornarTemplateIndex() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void buscaComCodigoValidoDeveRetornarTemplateResultado() throws Exception {
        mockMvc.perform(get("/viagem").param("codigo", "BR"))
                .andExpect(status().isOk())
                .andExpect(view().name("resultado"))
                .andExpect(model().attributeExists("overview"))
                .andExpect(model().attributeDoesNotExist("erro"));
    }

    @Test
    void buscaComCodigoInvalidoDeveRetornarErroAmigavel() throws Exception {
        mockMvc.perform(get("/viagem").param("codigo", "ZZ"))
                .andExpect(status().isOk())
                .andExpect(view().name("resultado"))
                .andExpect(model().attributeExists("erro"))
                .andExpect(model().attributeDoesNotExist("overview"));
    }

    @Test
    void buscaComCodigoVazioDeveRetornarMensagemDeErro() throws Exception {
        mockMvc.perform(get("/viagem").param("codigo", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("resultado"))
                .andExpect(model().attributeExists("erro"));
    }

    @Test
    void buscaDevePreencherAtributoCodigoBuscado() throws Exception {
        mockMvc.perform(get("/viagem").param("codigo", "jp"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("codigoBuscado", "JP"));
    }

    @Test
    void recomendacoesSemParamsDeveRedirecionarParaHome() throws Exception {
        mockMvc.perform(get("/recomendacoes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void recomendacoesComCountriesValidoDeveRetornarResultado() throws Exception {
        RecommendationResponse response = mockRecommendationResponse();

        when(recommendationEngine.recommend(any())).thenReturn(response);

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
        RecommendationResponse response = mockRecommendationResponse();

        when(recommendationEngine.recommend(any())).thenReturn(response);

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

    private RecommendationResponse mockRecommendationResponse() {
        return new RecommendationResponse(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                List.of(),
                List.of(new TravelRecommendation(
                        "JP",
                        "Japan",
                        68.0,
                        List.of(new ScoreEntry("CAMBIO", 35, 35, "ok")),
                        "JP — score 68: câmbio muito favorável"
                )),
                List.of()
        );
    }
}
