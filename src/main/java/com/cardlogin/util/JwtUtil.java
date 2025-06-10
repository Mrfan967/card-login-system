package com.cardlogin.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    private static final String SECRET_KEY = "card-login-secret-key";
    private static final long EXPIRATION = 1000 * 60 * 30; // 30分钟

    // 生成token
    public static String generateToken(String username, String cardNumber) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("cardNumber", cardNumber);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 解析token
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    // 校验token是否过期
    public static boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().before(new Date());
    }
} 