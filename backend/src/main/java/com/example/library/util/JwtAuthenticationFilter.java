package com.example.library.util;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT 身份验证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null) {
            Claims claims = jwtTokenUtil.validateToken(token);
            if (claims != null) {
                String username = claims.getSubject();
                Integer role = ((Number) claims.get("role")).intValue();
                String roleStr = getRoleString(role);

                // 创建 Authentication 对象并设置到 SecurityContext
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_" + roleStr)
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String getRoleString(Integer role) {
        if (role == null) {
            return "STUDENT";
        }
        return switch (role) {
            case 0 -> "ADMIN";
            case 1 -> "TEACHER";
            case 2 -> "STUDENT";
            default -> "STUDENT";
        };
    }
}
