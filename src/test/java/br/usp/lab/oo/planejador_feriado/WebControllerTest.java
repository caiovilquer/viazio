package br.usp.lab.oo.planejador_feriado;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.travel.model.TravelOverview;
import br.usp.lab.oo.planejador_feriado.travel.service.TravelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
}
