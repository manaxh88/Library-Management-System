package com.example.library.util;

import com.example.library.dto.ApiResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 权限检查的 AOP 拦截器
 */
@Aspect
@Component
public class RoleCheckAspect {

    private final JwtTokenUtil jwtTokenUtil;

    public RoleCheckAspect(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return ApiResponse.fail("请求上下文不存在");
        }

        HttpServletRequest request = attributes.getRequest();
        String token = extractToken(request);

        if (token == null) {
            return ApiResponse.fail("缺少身份验证令牌");
        }

        Claims claims = jwtTokenUtil.validateToken(token);
        if (claims == null) {
            return ApiResponse.fail("无效的身份验证令牌");
        }

        Integer userRole = ((Number) claims.get("role")).intValue();
        int[] allowedRoles = requireRole.roles();

        // 检查角色权限
        if (allowedRoles.length > 0) {
            boolean hasRole = false;
            for (int role : allowedRoles) {
                if (userRole == role) {
                    hasRole = true;
                    break;
                }
            }
            if (!hasRole) {
                return ApiResponse.fail("您没有权限访问此资源");
            }
        }

        return joinPoint.proceed();
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
