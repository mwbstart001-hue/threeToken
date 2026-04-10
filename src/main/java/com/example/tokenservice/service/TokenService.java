package com.example.tokenservice.service;

import com.example.tokenservice.config.SecurityConfig;
import com.example.tokenservice.model.TokenInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    private final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    private SecurityConfig securityConfig;

    public TokenService() {
        scheduler.scheduleAtFixedRate(this::cleanExpiredTokens, 5, 5, TimeUnit.MINUTES);
    }

    public TokenInfo generateToken(String userId) {
        return generateToken(userId, null);
    }

    public TokenInfo generateToken(String userId, String deviceId) {
        long createTime = System.currentTimeMillis();
        long expireTime = createTime + securityConfig.getExpireTime();
        
        String tokenId = java.util.UUID.randomUUID().toString();
        
        String token = Jwts.builder()
                .setId(tokenId)
                .setSubject(userId)
                .setIssuer(securityConfig.getIssuer())
                .setIssuedAt(new Date(createTime))
                .setExpiration(new Date(expireTime))
                .claim("deviceId", deviceId)
                .signWith(securityConfig.getSigningKey())
                .compact();
        
        TokenInfo tokenInfo = new TokenInfo(token, userId, createTime, expireTime);
        tokenInfo.setDeviceId(deviceId);
        tokenStore.put(token, tokenInfo);
        
        logger.info("Token generated for user: {}", userId);
        return tokenInfo;
    }

    public TokenInfo validateToken(String token) {
        return validateToken(token, null);
    }

    public TokenInfo validateToken(String token, String deviceId) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(securityConfig.getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            String userId = claims.getSubject();
            String tokenDeviceId = claims.get("deviceId", String.class);
            
            if (deviceId != null && !deviceId.equals(tokenDeviceId)) {
                logger.warn("Device ID mismatch for token. Expected: {}, Got: {}", tokenDeviceId, deviceId);
                return null;
            }
            
            TokenInfo tokenInfo = tokenStore.get(token);
            
            if (tokenInfo == null || !tokenInfo.isValid()) {
                return null;
            }
            
            if (System.currentTimeMillis() > tokenInfo.getExpireTime()) {
                tokenStore.remove(token);
                return null;
            }
            
            return tokenInfo;
            
        } catch (ExpiredJwtException e) {
            logger.warn("Token expired: {}", e.getMessage());
            tokenStore.remove(token);
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        
        return null;
    }

    public boolean invalidateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        TokenInfo tokenInfo = tokenStore.get(token);
        
        if (tokenInfo == null) {
            return false;
        }
        
        tokenInfo.setValid(false);
        tokenStore.remove(token);
        logger.info("Token invalidated for user: {}", tokenInfo.getUserId());
        
        return true;
    }

    private void cleanExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        int removedCount = 0;
        
        for (Map.Entry<String, TokenInfo> entry : tokenStore.entrySet()) {
            if (currentTime > entry.getValue().getExpireTime()) {
                tokenStore.remove(entry.getKey());
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            logger.info("Cleaned {} expired tokens", removedCount);
        }
    }

    public int getActiveTokenCount() {
        return tokenStore.size();
    }
}
