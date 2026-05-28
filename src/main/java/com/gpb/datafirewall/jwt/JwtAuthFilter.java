package com.gpb.datafirewall.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        List<String> protectedPaths = List.of(
                "/api/v1/*");

        String path = request.getServletPath() +
                (request.getPathInfo() != null ? request.getPathInfo() : "");

        if (!isProtectedPath(path, protectedPaths)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        if (token == null) {
            throw new RuntimeException("Token is missing!");
        }
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid token!");
        }
        filterChain.doFilter(request, response);
    }

    private boolean isProtectedPath(String path, List<String> unprotectedPaths) {
        for (String pattern : unprotectedPaths) {
            // Для точного совпадения
            if (path.equals(pattern)) {
                return true;
            }
            // Для /** wildcard
            if (pattern.endsWith("/**") && path.startsWith(pattern.substring(0, pattern.length() - 3))) {
                return true;
            }
            // Для /* PathVariable
            if (pattern.endsWith("/*") &&
                    path.startsWith(pattern.substring(0, pattern.length() - 2)) &&
                    path.substring(pattern.length() - 2).matches("/[^/]+")) {
                return true;
            }
        }
        return false;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
