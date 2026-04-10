package com.example.tokenservice.service;

import com.example.tokenservice.config.JwtConfig;
import com.example.tokenservice.model.TokenInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    @Autowired
    private JwtConfig jwtConfig;

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    private static final long CLEANUP_INTERVAL = 3600000;

    private volatile long lastCleanupTime = System.currentTimeMillis();

    public TokenInfo generateToken(String userId) {
        long createTime = System.currentTimeMillis();
        long expireTime = createTime + jwtConfig.getExpiration();
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .setId(jti)
                .setSubject(userId)
                .setIssuedAt(new Date(createTime))
                .setExpiration(new Date(expireTime))
                .claim("userId", userId)
                .signWith(jwtConfig.getSigningKey(), jwtConfig.getSignatureAlgorithm())
                .compact();

        return new TokenInfo(token, userId, createTime, expireTime, jti);
    }

    public TokenInfo validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        cleanupExpiredBlacklist();

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtConfig.getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String jti = claims.getId();
            if (blacklist.containsKey(jti)) {
                return null;
            }

            String userId = claims.getSubject();
            long createTime = claims.getIssuedAt().getTime();
            long expireTime = claims.getExpiration().getTime();

            return new TokenInfo(token, userId, createTime, expireTime, jti);

        } catch (SignatureException e) {
            return null;
        } catch (MalformedJwtException e) {
            return null;
        } catch (ExpiredJwtException e) {
            return null;
        } catch (UnsupportedJwtException e) {
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean invalidateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtConfig.getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String jti = claims.getId();
            long expireTime = claims.getExpiration().getTime();

            blacklist.put(jti, expireTime);
            return true;

        } catch (JwtException e) {
            return false;
        }
    }

    private void cleanupExpiredBlacklist() {
        long now = System.currentTimeMillis();
        if (now - lastCleanupTime < CLEANUP_INTERVAL) {
            return;
        }

        synchronized (this) {
            if (now - lastCleanupTime < CLEANUP_INTERVAL) {
                return;
            }

            blacklist.entrySet().removeIf(entry -> entry.getValue() < now);
            lastCleanupTime = now;
        }
    }

    public int getBlacklistSize() {
        return blacklist.size();
    }
}
