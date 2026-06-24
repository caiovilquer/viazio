package br.usp.lab.oo.planejador_feriado.meta.controller;

import br.usp.lab.oo.planejador_feriado.meta.dto.ApiLimits;
import br.usp.lab.oo.planejador_feriado.meta.dto.ApiMetaResponse;
import br.usp.lab.oo.planejador_feriado.meta.dto.RegionOption;
import br.usp.lab.oo.planejador_feriado.meta.service.ApiMetaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiMetaController.class)
class ApiMetaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApiMetaService metaService;

    @Test
    void returnsCacheableFrontendMetadata() throws Exception {
        when(metaService.getMetadata()).thenReturn(new ApiMetaResponse(
                "v1",
                "2026-06-23",
                new ApiLimits(92, 400, 15, 50, 60, 10),
                List.of(new RegionOption("Americas", "Américas")),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                Map.of("bestWindows", true)));

        mockMvc.perform(get("/api/v1/meta"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=86400, public"))
                .andExpect(jsonPath("$.apiVersion").value("v1"))
                .andExpect(jsonPath("$.regions[0].key").value("Americas"))
                .andExpect(jsonPath("$.capabilities.bestWindows").value(true));
    }
}
