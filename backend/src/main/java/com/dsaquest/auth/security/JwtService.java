package com.dsaquest.auth.security;

import com.dsaquest.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final Duration accessTokenTtl;

    public JwtService(
            @Value("${spring.security.jwt.secret}") String secret,
            @Value("${spring.security.jwt.access-token-ttl}") Duration accessTokenTtl) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = accessTokenTtl;
    }

    public Token createAccessToken(User user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(accessTokenTtl);
        String value = Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
        return new Token(value, expiresAt);
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record Token(String value, Instant expiresAt) {
    }
}
