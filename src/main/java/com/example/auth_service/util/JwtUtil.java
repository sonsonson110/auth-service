package com.example.auth_service.util;

import com.example.auth_service.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;


@Component
public class JwtUtil {
    public static enum JwtType {ACCESS, REFRESH}

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.refreshSecret}")
    private String refreshSecret;

    @Value("${jwt.access-token-expiration}")
    private int accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private int refreshTokenExpiration;

    private Key getSigningKey(JwtType type) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                type == JwtType.ACCESS ? jwtSecret : refreshSecret));
    }

    public String generateToken(User user, JwtType type) {
        final var builder = Jwts.builder();
        builder.setSubject(user.getId().toString())
                .claim("username", user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()));
        if (type == JwtType.ACCESS) {
            builder
                    .claim("role", user.getRole())
                    .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                    .signWith(getSigningKey(JwtType.ACCESS), SignatureAlgorithm.HS256);
        } else {
            builder.claim("sessionId", UUID.randomUUID().toString())
                    .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                    .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret)), SignatureAlgorithm.HS256);
        }
        return builder.compact();
    }

    public String extractUsername(String token, JwtType type) {
        return extractClaim(token, type, claims -> claims.get("username", String.class));
    }

    public String extractSessionId(String token) {
        return extractClaim(token, JwtType.REFRESH, claims -> claims.get("sessionId", String.class));
    }

    public <T> T extractClaim(String token, JwtType type, Function<Claims, T> claimsResolver) {
        final var claims = extractAllClaims(token, type);
        return claimsResolver.apply(claims);
    }

    // This will handle token validation first, then extract later
    private Claims extractAllClaims(String token, JwtType type) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(type))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
