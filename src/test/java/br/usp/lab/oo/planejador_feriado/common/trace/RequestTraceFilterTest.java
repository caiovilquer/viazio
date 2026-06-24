package br.usp.lab.oo.planejador_feriado.common.trace;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class RequestTraceFilterTest {

    private final RequestTraceFilter filter = new RequestTraceFilter();

    @Test
    void preservesSafeRequestIdAcrossRequestResponseAndLogs() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/meta");
        request.addHeader("X-Request-Id", "frontend-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertEquals("frontend-123", request.getAttribute(RequestTraceFilter.ATTRIBUTE));
        assertEquals("frontend-123", response.getHeader(RequestTraceFilter.RESPONSE_HEADER));
        assertNull(MDC.get("traceId"));
    }

    @Test
    void replacesUnsafeRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/meta");
        request.addHeader("X-Request-Id", "invalid header");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, mock(FilterChain.class));

        assertNotEquals("invalid header", response.getHeader(RequestTraceFilter.RESPONSE_HEADER));
    }
}
