package com.example.project.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    private final Key key;
    private final long accessTokenValiditySeconds;
    private final long refreshTokenValiditySeconds;

    public JwtUtils(
            @Value("${app.jwt.secret}") String secretBase64,
            @Value("${app.jwt.access-expiration-seconds:900}") long accessTokenSeconds,
            @Value("${app.jwt.refresh-expiration-seconds:604800}") long refreshTokenSeconds
    ) {
        // secretBase64 should be base64-encoded 256-bit key (32 bytes)
        this.key = Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secretBase64));
        this.accessTokenValiditySeconds = accessTokenSeconds;
        this.refreshTokenValiditySeconds = refreshTokenSeconds;
    }

//    public long getAccessTokenValiditySeconds() {
//        return accessTokenValiditySeconds;
//    }
//
//    public long getRefreshTokenValiditySeconds() {
//        return refreshTokenValiditySeconds;
//    }

    public String generateAccessToken(String username, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(accessTokenValiditySeconds, ChronoUnit.SECONDS)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(refreshTokenValiditySeconds, ChronoUnit.SECONDS)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> validateAndParse(String token) throws JwtException {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token);
    }

    public boolean isTokenExpired(String token) {
        try {
            Date exp = validateAndParse(token).getBody().getExpiration();
            return exp.before(new Date());
        } catch (JwtException ex) {
            return true;
        }
    }
}
