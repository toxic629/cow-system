package com.cowhealth.security;

import com.cowhealth.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.http.HttpMethod;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;

    public AuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(401, "Missing or invalid Authorization header");
        }
        String token = authHeader.substring("Bearer ".length());
        if (!tokenService.validToken(token)) {
            throw new BusinessException(401, "Token expired or invalid");
        }
        request.setAttribute("username", tokenService.getUsername(token));
        return true;
    }
}
