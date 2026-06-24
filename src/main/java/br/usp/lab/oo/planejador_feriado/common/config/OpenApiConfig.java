package br.usp.lab.oo.planejador_feriado.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI planejadorOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Planejador de Feriadões API")
                        .description("""
                                API de apoio à decisão para viagens curtas: combina calendário da origem,
                                clima, custo relativo, distância, festividades, qualidade dos dados e
                                estimativas transparentes de viabilidade. Use GET /api/v1/meta para montar
                                formulários sem valores hardcoded e POST /api/v1/recommendations para buscas
                                estruturadas.
                                """)
                        .version("v1")
                        .contact(new Contact().name("Grupo Laboo").url("https://gitlab.com/grupo-laboo/laboo_projeto"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
