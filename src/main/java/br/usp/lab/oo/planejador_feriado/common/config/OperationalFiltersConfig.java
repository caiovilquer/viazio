package br.usp.lab.oo.planejador_feriado.common.config;

import br.usp.lab.oo.planejador_feriado.common.security.SecurityHeadersFilter;
import br.usp.lab.oo.planejador_feriado.common.trace.RequestTraceFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class OperationalFiltersConfig {

  @Bean
  public FilterRegistrationBean<RequestTraceFilter> requestTraceFilter() {
    FilterRegistrationBean<RequestTraceFilter> registration =
      new FilterRegistrationBean<>(new RequestTraceFilter());
    registration.addUrlPatterns("/*");
    registration.setName("requestTraceFilter");
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }

  @Bean
  public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
    FilterRegistrationBean<SecurityHeadersFilter> registration =
      new FilterRegistrationBean<>(new SecurityHeadersFilter());
    registration.addUrlPatterns("/*");
    registration.setName("securityHeadersFilter");
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
    return registration;
  }
}
