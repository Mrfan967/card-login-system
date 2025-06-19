package com.cardlogin.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "card_login_system_secret_key_2024";
    private static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60 * 1000; // 24小时

    // 从token中提取用户名
    public static String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 从token中提取卡号
    public static String extractCardNumber(String token) {
        final Claims claims = extractAllClaims(token);
        return claims.get("cardNumber", String.class);
    }

    // 从token中提取过期时间
    public static Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 从token中提取指定的claim
    public static <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 从token中提取所有claims
    private static Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    // 检查token是否过期
    private static Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 生成token
    public static String generateToken(String username, String cardNumber) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("cardNumber", cardNumber);
        return createToken(claims, username);
    }

    // 创建token
    private static String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 验证token
    public static Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
} 