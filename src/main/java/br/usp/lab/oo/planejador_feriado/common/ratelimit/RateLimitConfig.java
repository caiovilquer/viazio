package br.usp.lab.oo.planejador_feriado.common.ratelimit;

import br.usp.lab.oo.planejador_feriado.common.config.RateLimitProperties;
import br.usp.lab.oo.planejador_feriado.common.config.WebConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class RateLimitConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(
            RateLimitProperties properties,
            ObjectMapper objectMapper) {
        FilterRegistrationBean<RateLimitFilter> registration =
                new FilterRegistrationBean<>(new RateLimitFilter(properties, objectMapper));
        registration.addUrlPatterns(WebConfig.API_PREFIX + "/*");
        registration.setName("rateLimitFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return registration;
    }
}
