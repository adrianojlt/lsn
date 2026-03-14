package com.lsn.server.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET      = "lsn-default-secret-key-change-in-production-32c";
    private static final long   EXPIRY_MS   = 86_400_000L;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, EXPIRY_MS);
    }

    @Test
    void generateToken_extractUsername_returnsSubject() {
        String token = jwtUtil.generateToken("alice");

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("alice");
    }

    @Test
    void isValid_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("alice");

        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    void isValid_tamperedToken_returnsFalse() {
        String token   = jwtUtil.generateToken("alice");
        String tampered = token.substring(0, token.length() - 4) + "xxxx";

        assertThat(jwtUtil.isValid(tampered)).isFalse();
    }

    @Test
    void isValid_expiredToken_returnsFalse() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        String expiredToken = Jwts.builder()
                .subject("alice")
                .issuedAt(new Date(0))
                .expiration(new Date(1))
                .signWith(key)
                .compact();

        assertThat(jwtUtil.isValid(expiredToken)).isFalse();
    }
}
