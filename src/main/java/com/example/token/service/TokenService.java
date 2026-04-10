package com.example.token.service;

import com.example.token.model.TokenInfo;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    private final ConcurrentHashMap<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();

    private static final long DEFAULT_EXPIRE_HOURS = 24;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TokenService() {
        scheduler.scheduleAtFixedRate(this::cleanExpiredTokens, 1, 1, TimeUnit.HOURS);
    }

    public String generateToken(String userId) {
        return generateToken(userId, DEFAULT_EXPIRE_HOURS);
    }

    public String generateToken(String userId, long expireHours) {
        String token = UUID.randomUUID().toString().replace("-", "");
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + expireHours * 60 * 60 * 1000);

        TokenInfo tokenInfo = new TokenInfo(token, userId, now, expireTime);
        tokenStore.put(token, tokenInfo);

        return token;
    }

    public TokenInfo validateToken(String token) {
        TokenInfo tokenInfo = tokenStore.get(token);

        if (tokenInfo == null) {
            return null;
        }

        if (tokenInfo.isRevoked()) {
            return null;
        }

        if (tokenInfo.isExpired()) {
            tokenStore.remove(token);
            return null;
        }

        return tokenInfo;
    }

    public boolean revokeToken(String token) {
        TokenInfo tokenInfo = tokenStore.get(token);
        if (tokenInfo == null) {
            return false;
        }
        tokenInfo.setRevoked(true);
        tokenStore.put(token, tokenInfo);
        return true;
    }

    public boolean isValid(String token) {
        return validateToken(token) != null;
    }

    private void cleanExpiredTokens() {
        long now = System.currentTimeMillis();
        tokenStore.entrySet().removeIf(entry -> {
            TokenInfo info = entry.getValue();
            return info.getExpireTime().getTime() < now || info.isRevoked();
        });
    }

    public int getTokenCount() {
        return tokenStore.size();
    }
}
