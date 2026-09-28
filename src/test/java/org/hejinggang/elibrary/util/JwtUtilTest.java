package org.hejinggang.elibrary.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @Test
    void generateUserToken_success() {
        String token = JwtUtil.generateUserToken(1L, "testuser", "NORMAL");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateAdminToken_success() {
        String token = JwtUtil.generateAdminToken(1L, "admin", "ADMIN");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void parseUserToken_success() {
        String token = JwtUtil.generateUserToken(1L, "testuser", "NORMAL");

        Claims claims = JwtUtil.parseToken(token);

        assertEquals(1L, claims.get("userId", Long.class));
        assertEquals("testuser", claims.get("username", String.class));
        assertEquals("NORMAL", claims.get("userType", String.class));
    }

    @Test
    void parseAdminToken_success() {
        String token = JwtUtil.generateAdminToken(1L, "admin", "ADMIN");

        Claims claims = JwtUtil.parseToken(token);

        assertEquals(1L, claims.get("adminId", Long.class));
        assertEquals("admin", claims.get("username", String.class));
        assertEquals("ADMIN", claims.get("adminRole", String.class));
    }

    @Test
    void parseInvalidToken_throwsException() {
        assertThrows(Exception.class, () -> JwtUtil.parseToken("invalid.token.here"));
    }

    @Test
    void parseExpiredToken_throwsException() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZXhwIjoxMDAwMDAwMDAwfQ.invalid";
        assertThrows(Exception.class, () -> JwtUtil.parseToken(token));
    }
}