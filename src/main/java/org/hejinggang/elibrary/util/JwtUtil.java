package org.hejinggang.elibrary.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    // 开发环境使用固定密钥，生产环境应从配置中心获取
    private static final String SECRET_KEY_STRING = "elibrary-jwt-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm!";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));
    private static final long USER_TOKEN_EXPIRE_MS = 24 * 60 * 60 * 1000;
    private static final long ADMIN_TOKEN_EXPIRE_MS = 8 * 60 * 60 * 1000;

    public static String generateUserToken(Long userId, String username, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("userType", userType);
        
        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + USER_TOKEN_EXPIRE_MS))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static String generateAdminToken(Long adminId, String username, String adminRole) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", adminId);
        claims.put("username", username);
        claims.put("adminRole", adminRole);
        
        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(adminId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ADMIN_TOKEN_EXPIRE_MS))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}