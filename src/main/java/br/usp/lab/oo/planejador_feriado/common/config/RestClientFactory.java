package br.usp.lab.oo.planejador_feriado.common.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Centraliza a criação dos {@link RestClient} usados pelos clients de APIs externas,
 * garantindo timeouts de conexão e leitura consistentes em todos eles.
 */
@Component
public class RestClientFactory {

  /**
   * Algumas APIs (ex.: Wikimedia REST API) retornam 403 para requisições sem um
   * User-Agent identificável, bloqueando o User-Agent padrão do {@code HttpURLConnection}
   * do Java. Ver https://meta.wikimedia.org/wiki/User-Agent_policy.
   */
  private static final String USER_AGENT =
    "planejador-feriado/0.0.1 (+https://gitlab.com/grupo-laboo/laboo_projeto)";

  private final ExternalApisProperties properties;

  public RestClientFactory(ExternalApisProperties properties) {
    this.properties = properties;
  }

  public RestClient.Builder builderFor(String baseUrl) {
    SimpleClientHttpRequestFactory requestFactory =
      new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(
      (int) properties.connectTimeout().toMillis()
    );
    requestFactory.setReadTimeout((int) properties.readTimeout().toMillis());

    return RestClient.builder()
      .baseUrl(baseUrl)
      .requestFactory(requestFactory)
      .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT);
  }
}
