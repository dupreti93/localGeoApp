package com.localapp.config;

import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filter to implement rate limiting per IP address.
 * Limits each IP to 100 requests per minute.
 */
public class RateLimitFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = httpRequest.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(100)
                        .refillGreedy(100, Duration.ofMinutes(1)))
                .build());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            logger.warn("Rate limit exceeded for IP: {}", clientIp);
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.getWriter().write("Too Many Requests");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization needed
    }

    @Override
    public void destroy() {
        buckets.clear();
    }
}