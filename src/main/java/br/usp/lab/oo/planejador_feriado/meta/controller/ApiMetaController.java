package br.usp.lab.oo.planejador_feriado.meta.controller;

import br.usp.lab.oo.planejador_feriado.meta.dto.ApiMetaResponse;
import br.usp.lab.oo.planejador_feriado.meta.service.ApiMetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meta")
@Tag(
  name = "Metadados",
  description = "Catálogos e limites necessários para montar clientes da API"
)
public class ApiMetaController {

  private final ApiMetaService metaService;

  public ApiMetaController(ApiMetaService metaService) {
    this.metaService = metaService;
  }

  @GetMapping
  @Operation(
    summary = "Obtém países, cidades, perfis, critérios, limites e capacidades"
  )
  public ResponseEntity<ApiMetaResponse> getMetadata() {
    return ResponseEntity.ok()
      .cacheControl(CacheControl.maxAge(Duration.ofHours(24)).cachePublic())
      .body(metaService.getMetadata());
  }
}
