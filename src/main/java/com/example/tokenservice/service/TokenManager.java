package com.example.tokenservice.service;

import com.example.tokenservice.entity.TokenInfo;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenManager {

    @Value("${token.default-expire-minutes:120}")
    private int defaultExpireMinutes;

    private final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();

    public String generateToken(String userId) {
        return generateToken(userId, defaultExpireMinutes);
    }

    public String generateToken(String userId, int expireMinutes) {
        String token = RandomStringUtils.randomAlphanumeric(32);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusMinutes(expireMinutes);
        TokenInfo tokenInfo = new TokenInfo(token, userId, now, expireTime);
        tokenStore.put(token, tokenInfo);
        return token;
    }

    public boolean validateToken(String token) {
        TokenInfo tokenInfo = tokenStore.get(token);
        return tokenInfo != null && tokenInfo.isValid();
    }

    public boolean revokeToken(String token) {
        TokenInfo tokenInfo = tokenStore.get(token);
        if (tokenInfo != null) {
            tokenInfo.setRevoked(true);
            return true;
        }
        return false;
    }

    public TokenInfo getTokenInfo(String token) {
        return tokenStore.get(token);
    }
}
