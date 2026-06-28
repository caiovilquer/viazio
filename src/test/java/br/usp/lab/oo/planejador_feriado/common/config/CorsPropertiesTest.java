package br.usp.lab.oo.planejador_feriado.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class CorsPropertiesTest {

  @Test
  void splitsCommaSeparatedOrigins() {
    CorsProperties properties = new CorsProperties(
      List.of(
        "https://viazio.vercel.app,https://viazio-git-main.vercel.app"
      )
    );

    assertEquals(2, properties.allowedOrigins().size());
    assertTrue(properties.allowedOrigins().contains("https://viazio.vercel.app"));
    assertTrue(
      properties.allowedOrigins().contains("https://viazio-git-main.vercel.app")
    );
  }

  @Test
  void ignoresBlankOrigins() {
    CorsProperties properties = new CorsProperties(List.of("  ", ""));

    assertTrue(properties.allowedOrigins().isEmpty());
  }
}
