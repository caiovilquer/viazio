package br.usp.lab.oo.planejador_feriado.common.ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import br.usp.lab.oo.planejador_feriado.common.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

  @Test
  void fallsBackToSafeDefaultsForInvalidConfiguration() {
    RateLimitProperties properties = new RateLimitProperties(0, -1, 0, false);

    assertEquals(60, properties.capacityOrDefault());
    assertEquals(60, properties.refillPerMinuteOrDefault());
    assertEquals(10_000, properties.maximumClientsOrDefault());
  }

  @Test
  void allowsRequestsWithinCapacity() throws Exception {
    RateLimitFilter filter = new RateLimitFilter(new RateLimitProperties(2, 2));
    FilterChain chain = mock(FilterChain.class);

    for (int i = 0; i < 2; i++) {
      MockHttpServletRequest request = new MockHttpServletRequest(
        "GET",
        "/api/v1/exchange/USD"
      );
      request.setRemoteAddr("10.0.0.1");
      MockHttpServletResponse response = new MockHttpServletResponse();
      filter.doFilter(request, response, chain);
      assertEquals(200, response.getStatus());
      assertEquals("2", response.getHeader("X-RateLimit-Limit"));
    }
    verify(chain, times(2)).doFilter(
      any(ServletRequest.class),
      any(ServletResponse.class)
    );
  }

  @Test
  void blocksRequestsBeyondCapacity() throws Exception {
    RateLimitFilter filter = new RateLimitFilter(new RateLimitProperties(1, 1));
    FilterChain chain = mock(FilterChain.class);

    MockHttpServletRequest first = new MockHttpServletRequest(
      "GET",
      "/api/v1/exchange/USD"
    );
    first.setRemoteAddr("10.0.0.2");
    MockHttpServletResponse firstResponse = new MockHttpServletResponse();
    filter.doFilter(first, firstResponse, chain);

    MockHttpServletRequest second = new MockHttpServletRequest(
      "GET",
      "/api/v1/exchange/USD"
    );
    second.setRemoteAddr("10.0.0.2");
    MockHttpServletResponse secondResponse = new MockHttpServletResponse();
    filter.doFilter(second, secondResponse, chain);

    assertEquals(200, firstResponse.getStatus());
    assertEquals(429, secondResponse.getStatus());
    assertEquals("60", secondResponse.getHeader("Retry-After"));
    assertTrue(
      secondResponse
        .getContentAsString()
        .contains("\"code\":\"RATE_LIMIT_EXCEEDED\"")
    );
    assertTrue(secondResponse.getContentAsString().contains("\"traceId\""));
    verify(chain, times(1)).doFilter(
      any(ServletRequest.class),
      any(ServletResponse.class)
    );
  }

  @Test
  void tracksDifferentClientsIndependently() throws Exception {
    RateLimitFilter filter = new RateLimitFilter(new RateLimitProperties(1, 1));
    FilterChain chain = mock(FilterChain.class);

    MockHttpServletRequest clientA = new MockHttpServletRequest(
      "GET",
      "/api/v1/exchange/USD"
    );
    clientA.setRemoteAddr("10.0.0.3");
    MockHttpServletResponse responseA = new MockHttpServletResponse();
    filter.doFilter(clientA, responseA, chain);

    MockHttpServletRequest clientB = new MockHttpServletRequest(
      "GET",
      "/api/v1/exchange/USD"
    );
    clientB.setRemoteAddr("10.0.0.4");
    MockHttpServletResponse responseB = new MockHttpServletResponse();
    filter.doFilter(clientB, responseB, chain);

    assertEquals(200, responseA.getStatus());
    assertEquals(200, responseB.getStatus());
  }

  @Test
  void ignoresForwardedHeaderUnlessProxyTrustIsEnabled() throws Exception {
    RateLimitFilter filter = new RateLimitFilter(new RateLimitProperties(1, 1));
    FilterChain chain = mock(FilterChain.class);

    MockHttpServletRequest first = request("10.0.0.5", "198.51.100.1");
    MockHttpServletResponse firstResponse = new MockHttpServletResponse();
    filter.doFilter(first, firstResponse, chain);

    MockHttpServletRequest second = request("10.0.0.5", "198.51.100.2");
    MockHttpServletResponse secondResponse = new MockHttpServletResponse();
    filter.doFilter(second, secondResponse, chain);

    assertEquals(200, firstResponse.getStatus());
    assertEquals(429, secondResponse.getStatus());
  }

  @Test
  void honorsForwardedHeaderBehindExplicitlyTrustedProxy() throws Exception {
    RateLimitFilter filter = new RateLimitFilter(
      new RateLimitProperties(1, 1, 100, true)
    );
    FilterChain chain = mock(FilterChain.class);

    MockHttpServletResponse firstResponse = new MockHttpServletResponse();
    filter.doFilter(request("10.0.0.5", "198.51.100.1"), firstResponse, chain);
    MockHttpServletResponse secondResponse = new MockHttpServletResponse();
    filter.doFilter(request("10.0.0.5", "198.51.100.2"), secondResponse, chain);

    assertEquals(200, firstResponse.getStatus());
    assertEquals(200, secondResponse.getStatus());
  }

  @Test
  void doesNotRateLimitCorsPreflight() throws Exception {
    RateLimitFilter filter = new RateLimitFilter(new RateLimitProperties(1, 1));
    FilterChain chain = mock(FilterChain.class);
    MockHttpServletRequest request = new MockHttpServletRequest(
      "OPTIONS",
      "/api/v1/recommendations"
    );
    request.setRemoteAddr("10.0.0.6");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chain);

    assertEquals(200, response.getStatus());
    verify(chain, times(1)).doFilter(
      any(ServletRequest.class),
      any(ServletResponse.class)
    );
  }

  @Test
  void boundsClientBucketCache() throws Exception {
    RateLimitFilter filter = new RateLimitFilter(
      new RateLimitProperties(10, 10, 2, false)
    );
    FilterChain chain = mock(FilterChain.class);

    for (int i = 0; i < 10; i++) {
      MockHttpServletRequest request = new MockHttpServletRequest(
        "GET",
        "/api/v1/meta"
      );
      request.setRemoteAddr("10.0.1." + i);
      filter.doFilter(request, new MockHttpServletResponse(), chain);
    }

    assertTrue(filter.estimatedClientCount() <= 2);
  }

  private MockHttpServletRequest request(
    String remoteAddress,
    String forwardedFor
  ) {
    MockHttpServletRequest request = new MockHttpServletRequest(
      "GET",
      "/api/v1/meta"
    );
    request.setRemoteAddr(remoteAddress);
    request.addHeader("X-Forwarded-For", forwardedFor);
    return request;
  }
}
