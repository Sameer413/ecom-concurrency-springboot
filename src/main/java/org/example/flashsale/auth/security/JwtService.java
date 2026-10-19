package org.example.flashsale.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.flashsale.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey secretKey;

    private final long accessTokenExpiration;

    private final long refreshTokenExpiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration

    ) {
        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );

        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole())
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + accessTokenExpiration)
                )
                .signWith(secretKey) // Why can't we pass the H256 Algo to the "signWith" argument
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
//                .claim("role", user.getRole())
                .claim("type", "refresh")
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + refreshTokenExpiration)
                )
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {
        String subject = parseToken(token).getSubject();

        return UUID.fromString(subject);
    }

    public String extractTokenType(String token) {

        return parseToken(token)
                .get("type", String.class);
    }

    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
