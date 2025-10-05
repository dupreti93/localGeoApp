package com.localapp.config;

import com.localapp.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        logger.info("Processing request: {} {} from origin: {}",
                request.getMethod(), request.getRequestURI(), request.getHeader("Origin"));
        logger.debug("Request headers: Authorization={}, Content-Type={}",
                request.getHeader("Authorization"), request.getHeader("Content-Type"));

        // Skip JWT validation for OPTIONS requests and /api/auth/** endpoints
        if (request.getMethod().equals("OPTIONS") || request.getRequestURI().startsWith("/api/auth/")) {
            logger.info("Skipping JWT validation for {} request to {}",
                    request.getMethod(), request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        logger.debug("Authorization header: {}", header);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validateToken(token)) {
                String userId = jwtUtil.extractUserId(token);
                logger.info("Token validated, userId: {}", userId);

                // Create authentication with proper authorities - THIS IS THE FIX!
                java.util.List<org.springframework.security.core.GrantedAuthority> authorities =
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"));

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.info("Authentication set with ROLE_USER for userId: {}", userId);
            } else {
                logger.warn("Invalid JWT token");
            }
        } else {
            logger.debug("No Bearer token found in Authorization header");
        }

        chain.doFilter(request, response);
        logger.info("Completed processing request: {} {}, response status: {}",
                request.getMethod(), request.getRequestURI(), response.getStatus());
    }
}