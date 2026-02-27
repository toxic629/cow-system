package com.cowhealth.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = request.getHeader("X-Trace-Id");
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        TraceIdHolder.setTraceId(traceId);
        MDC.put("traceId", traceId);
        response.setHeader("X-Trace-Id", traceId);
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info("request method={} uri={} status={} costMs={} traceId={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(),
                    System.currentTimeMillis() - start, traceId);
            MDC.remove("traceId");
            TraceIdHolder.clear();
        }
    }
}
