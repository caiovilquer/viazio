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
                        .description("API para consultar países, feriados e câmbio, e gerar recomendações de destinos para viagens curtas a partir de feriados/feriadões.")
                        .version("v1")
                        .contact(new Contact().name("Grupo Laboo").url("https://gitlab.com/grupo-laboo/laboo_projeto"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}
