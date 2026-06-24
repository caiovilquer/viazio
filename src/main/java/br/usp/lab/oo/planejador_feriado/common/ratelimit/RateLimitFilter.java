package br.usp.lab.oo.planejador_feriado.common.ratelimit;

import br.usp.lab.oo.planejador_feriado.common.config.RateLimitProperties;
import br.usp.lab.oo.planejador_feriado.common.exception.ApiError;
import br.usp.lab.oo.planejador_feriado.common.exception.ApiViolation;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Limita o número de requisições por IP nos endpoints {@code /api/v1/**}, protegendo
 * a API (e, por consequência, as cotas das APIs externas gratuitas que ela consome)
 * contra uso abusivo. Não depende de autenticação, já que o projeto opera sem login.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties properties) {
        this(properties, new ObjectMapper().findAndRegisterModules());
    }

    public RateLimitFilter(RateLimitProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Bucket bucket = buckets.computeIfAbsent(clientKey(request), key -> newBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
            return;
        }

        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.RETRY_AFTER, "60");
        objectMapper.writeValue(response.getWriter(), new ApiError(
                Instant.now(),
                429,
                "Too Many Requests",
                "RATE_LIMIT_EXCEEDED",
                "Limite de requisições excedido, tente novamente em breve",
                request.getRequestURI(),
                UUID.randomUUID().toString(),
                List.<ApiViolation>of()));
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(
                properties.capacityOrDefault(),
                Refill.greedy(properties.refillPerMinuteOrDefault(), Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private String clientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
