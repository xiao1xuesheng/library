package org.hejinggang.elibrary.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestUri = httpRequest.getRequestURI();

        String traceId = httpRequest.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put("traceId", traceId);

        if (requestUri.startsWith("/bos/")) {
            String adminId = httpRequest.getHeader("X-Admin-Id");
            if (adminId == null || adminId.isEmpty()) {
                throw new SecurityException("BOS接口需要管理员鉴权，请提供X-Admin-Id请求头");
            }
            MDC.put("adminId", adminId);

            String adminRole = httpRequest.getHeader("X-Admin-Role");
            MDC.put("adminRole", adminRole != null ? adminRole : "ADMIN");
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}