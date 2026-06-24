package br.usp.lab.oo.planejador_feriado.common.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SecurityHeadersFilterTest {

    private final SecurityHeadersFilter filter = new SecurityHeadersFilter();

    @Test
    void addsBrowserSecurityHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertEquals("DENY", response.getHeader("X-Frame-Options"));
        assertEquals("no-referrer", response.getHeader("Referrer-Policy"));
        assertTrue(response.getHeader("Content-Security-Policy").contains("frame-ancestors 'none'"));
        assertNull(response.getHeader("Strict-Transport-Security"));
    }

    @Test
    void addsHstsOnlyForHttps() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        request.setSecure(true);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertTrue(response.getHeader("Strict-Transport-Security").contains("max-age=31536000"));
    }
}
