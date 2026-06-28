package br.usp.lab.oo.planejador_feriado.common.ratelimit;

import br.usp.lab.oo.planejador_feriado.common.config.RateLimitProperties;
import br.usp.lab.oo.planejador_feriado.common.exception.ApiError;
import br.usp.lab.oo.planejador_feriado.common.exception.ApiViolation;
import br.usp.lab.oo.planejador_feriado.common.trace.RequestTraceFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Limita o número de requisições por IP nos endpoints {@code /api/v1/**}, protegendo
 * a API (e, por consequência, as cotas das APIs externas gratuitas que ela consome)
 * contra uso abusivo. Não depende de autenticação, já que o projeto opera sem login.
 */
public class RateLimitFilter extends OncePerRequestFilter {

  private final RateLimitProperties properties;
  private final ObjectMapper objectMapper;
  private final Cache<String, Bucket> buckets;

  public RateLimitFilter(RateLimitProperties properties) {
    this(properties, new ObjectMapper().findAndRegisterModules());
  }

  public RateLimitFilter(
    RateLimitProperties properties,
    ObjectMapper objectMapper
  ) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.buckets = Caffeine.newBuilder()
      .maximumSize(properties.maximumClientsOrDefault())
      .expireAfterAccess(Duration.ofMinutes(15))
      .build();
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain chain
  ) throws ServletException, IOException {
    Bucket bucket = buckets.get(clientKey(request), key -> newBucket());

    if (bucket.tryConsume(1)) {
      addRateLimitHeaders(response, bucket);
      chain.doFilter(request, response);
      return;
    }

    response.setStatus(429);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    addRateLimitHeaders(response, bucket);
    response.setHeader(
      HttpHeaders.RETRY_AFTER,
      String.valueOf(retryAfterSeconds())
    );
    objectMapper.writeValue(
      response.getWriter(),
      new ApiError(
        Instant.now(),
        429,
        "Too Many Requests",
        "RATE_LIMIT_EXCEEDED",
        "Limite de requisições excedido, tente novamente em breve",
        request.getRequestURI(),
        traceId(request),
        List.<ApiViolation>of()
      )
    );
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return "OPTIONS".equalsIgnoreCase(request.getMethod());
  }

  private void addRateLimitHeaders(
    HttpServletResponse response,
    Bucket bucket
  ) {
    response.setHeader(
      "X-RateLimit-Limit",
      String.valueOf(properties.capacityOrDefault())
    );
    response.setHeader(
      "X-RateLimit-Remaining",
      String.valueOf(bucket.getAvailableTokens())
    );
  }

  private long retryAfterSeconds() {
    return Math.max(
      1L,
      (long) Math.ceil(60.0 / properties.refillPerMinuteOrDefault())
    );
  }

  private Bucket newBucket() {
    Bandwidth limit = Bandwidth.classic(
      properties.capacityOrDefault(),
      Refill.greedy(
        properties.refillPerMinuteOrDefault(),
        Duration.ofMinutes(1)
      )
    );
    return Bucket.builder().addLimit(limit).build();
  }

  private String clientKey(HttpServletRequest request) {
    if (properties.trustForwardedHeadersOrDefault()) {
      String forwardedFor = request.getHeader("X-Forwarded-For");
      if (forwardedFor != null && !forwardedFor.isBlank()) {
        return forwardedFor.split(",")[0].trim();
      }
    }
    return request.getRemoteAddr();
  }

  private String traceId(HttpServletRequest request) {
    Object value = request.getAttribute(RequestTraceFilter.ATTRIBUTE);
    return value instanceof String traceId && !traceId.isBlank()
      ? traceId
      : UUID.randomUUID().toString();
  }

  long estimatedClientCount() {
    buckets.cleanUp();
    return buckets.estimatedSize();
  }
}
