package br.usp.lab.oo.planejador_feriado.common.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

public class RequestTraceFilter extends OncePerRequestFilter {

    public static final String ATTRIBUTE = RequestTraceFilter.class.getName() + ".traceId";
    public static final String RESPONSE_HEADER = "X-Trace-Id";
    private static final String REQUEST_HEADER = "X-Request-Id";
    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String traceId = resolveTraceId(request.getHeader(REQUEST_HEADER));
        request.setAttribute(ATTRIBUTE, traceId);
        response.setHeader(RESPONSE_HEADER, traceId);
        MDC.put("traceId", traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("traceId");
        }
    }

    private String resolveTraceId(String requestedId) {
        if (requestedId != null && SAFE_REQUEST_ID.matcher(requestedId).matches()) {
            return requestedId;
        }
        return UUID.randomUUID().toString();
    }
}
