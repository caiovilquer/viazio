package br.usp.lab.oo.planejador_feriado.common.ratelimit;

import br.usp.lab.oo.planejador_feriado.common.config.RateLimitProperties;
import br.usp.lab.oo.planejador_feriado.common.config.WebConfig;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(RateLimitProperties properties) {
        FilterRegistrationBean<RateLimitFilter> registration =
                new FilterRegistrationBean<>(new RateLimitFilter(properties));
        registration.addUrlPatterns(WebConfig.API_PREFIX + "/*");
        registration.setName("rateLimitFilter");
        return registration;
    }
}
