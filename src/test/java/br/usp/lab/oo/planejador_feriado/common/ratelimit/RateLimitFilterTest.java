package br.usp.lab.oo.planejador_feriado.common.ratelimit;

import br.usp.lab.oo.planejador_feriado.common.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RateLimitFilterTest {

    @Test
    void allowsRequestsWithinCapacity() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(new RateLimitProperties(2, 2));
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/exchange/USD");
            request.setRemoteAddr("10.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, chain);
            assertEquals(200, response.getStatus());
        }
        verify(chain, times(2)).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void blocksRequestsBeyondCapacity() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(new RateLimitProperties(1, 1));
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletRequest first = new MockHttpServletRequest("GET", "/api/v1/exchange/USD");
        first.setRemoteAddr("10.0.0.2");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        filter.doFilter(first, firstResponse, chain);

        MockHttpServletRequest second = new MockHttpServletRequest("GET", "/api/v1/exchange/USD");
        second.setRemoteAddr("10.0.0.2");
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        filter.doFilter(second, secondResponse, chain);

        assertEquals(200, firstResponse.getStatus());
        assertEquals(429, secondResponse.getStatus());
        verify(chain, times(1)).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void tracksDifferentClientsIndependently() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(new RateLimitProperties(1, 1));
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletRequest clientA = new MockHttpServletRequest("GET", "/api/v1/exchange/USD");
        clientA.setRemoteAddr("10.0.0.3");
        MockHttpServletResponse responseA = new MockHttpServletResponse();
        filter.doFilter(clientA, responseA, chain);

        MockHttpServletRequest clientB = new MockHttpServletRequest("GET", "/api/v1/exchange/USD");
        clientB.setRemoteAddr("10.0.0.4");
        MockHttpServletResponse responseB = new MockHttpServletResponse();
        filter.doFilter(clientB, responseB, chain);

        assertEquals(200, responseA.getStatus());
        assertEquals(200, responseB.getStatus());
    }
}
