package com.cardlogin.config;

import com.cardlogin.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("未登录或token缺失");
            return false;
        }
        
        String token = authHeader.substring(7);
        try {
            // 提取用户名和卡号
            String username = JwtUtil.extractUsername(token);
            String cardNumber = JwtUtil.extractCardNumber(token);
            
            // 验证token是否过期
            if (JwtUtil.extractExpiration(token).before(new java.util.Date())) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("token已过期");
                return false;
            }
            
            // 将信息存入request attribute供后续使用
            request.setAttribute("username", username);
            request.setAttribute("cardNumber", cardNumber);
            return true;
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("token已过期");
            return false;
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("token无效: " + e.getMessage());
            return false;
        }
    }
} 