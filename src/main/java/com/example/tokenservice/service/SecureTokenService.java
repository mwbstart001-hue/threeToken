package com.example.tokenservice.service;

import com.example.tokenservice.model.TokenInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SecureTokenService {

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}")
    private long expirationTime;

    @Value("${jwt.refresh-expiration:86400000}")
    private long refreshExpirationTime;

    private SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();
    private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            byte[] keyBytes = new byte[64];
            secureRandom.nextBytes(keyBytes);
            String generatedSecret = Base64.getEncoder().encodeToString(keyBytes);
            this.secretKey = Keys.hmacShaKeyFor(generatedSecret.getBytes(StandardCharsets.UTF_8));
        } else {
            this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        }
    }

    public TokenInfo generateToken(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId cannot be empty");
        }

        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date accessTokenExpiry = new Date(now + expirationTime);
        Date refreshTokenExpiry = new Date(now + refreshExpirationTime);

        String jti = generateSecureJti();

        String accessToken = Jwts.builder()
                .setSubject(userId)
                .setId(jti)
                .setIssuedAt(issuedAt)
                .setExpiration(accessTokenExpiry)
                .claim("type", "access")
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(userId)
                .setId(generateSecureJti())
                .setIssuedAt(issuedAt)
                .setExpiration(refreshTokenExpiry)
                .claim("type", "refresh")
                .claim("access_jti", jti)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        refreshTokenStore.put(refreshToken, userId);

        return new TokenInfo(accessToken, refreshToken, userId, now, accessTokenExpiry.getTime());
    }

    public TokenInfo refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return null;
        }

        if (!refreshToken.contains(".") || refreshToken.split("\\.").length != 3) {
            return null;
        }

        try {
            Claims claims = parseToken(refreshToken);

            if (claims == null || !"refresh".equals(claims.get("type"))) {
                return null;
            }

            if (revokedTokens.contains(refreshToken)) {
                return null;
            }

            String userId = claims.getSubject();
            String storedUserId = refreshTokenStore.get(refreshToken);

            if (storedUserId == null || !storedUserId.equals(userId)) {
                return null;
            }

            revokedTokens.add(refreshToken);
            refreshTokenStore.remove(refreshToken);

            return generateToken(userId);

        } catch (ExpiredJwtException e) {
            revokedTokens.add(refreshToken);
            refreshTokenStore.remove(refreshToken);
            return null;
        }
    }

    public TokenInfo validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        try {
            Claims claims = parseToken(token);

            if (claims == null || !"access".equals(claims.get("type"))) {
                return null;
            }

            if (revokedTokens.contains(token)) {
                return null;
            }

            String userId = claims.getSubject();
            long expireTime = claims.getExpiration().getTime();

            return new TokenInfo(token, null, userId, claims.getIssuedAt().getTime(), expireTime);

        } catch (ExpiredJwtException e) {
            return null;
        } catch (JwtException e) {
            return null;
        }
    }

    public boolean invalidateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            Claims claims = parseToken(token);
            if (claims != null) {
                revokedTokens.add(token);

                if ("refresh".equals(claims.get("type"))) {
                    refreshTokenStore.remove(token);
                }
                return true;
            }
        } catch (JwtException e) {
            // Token invalid or expired
        }

        return false;
    }

    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String generateSecureJti() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public boolean isTokenRevoked(String token) {
        return revokedTokens.contains(token);
    }

    public void cleanupExpiredTokens() {
        revokedTokens.clear();
    }
}
