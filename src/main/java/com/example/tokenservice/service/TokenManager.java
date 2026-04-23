package com.example.tokenservice.service;

import com.example.tokenservice.entity.TokenInfo;
import com.example.tokenservice.exception.TokenException;
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

    @Value("${token.refresh-window-minutes:60}")
    private int refreshWindowMinutes;

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

    public String refreshToken(String oldToken) {
        TokenInfo oldTokenInfo = tokenStore.get(oldToken);

        if (oldTokenInfo == null) {
            throw new TokenException(TokenException.ErrorCode.TOKEN_NOT_FOUND);
        }

        if (oldTokenInfo.isRevoked()) {
            throw new TokenException(TokenException.ErrorCode.TOKEN_REVOKED);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = oldTokenInfo.getExpireTime();

        if (now.isAfter(expireTime)) {
            throw new TokenException(TokenException.ErrorCode.TOKEN_EXPIRED);
        }

        LocalDateTime refreshWindowStart = expireTime.minusMinutes(refreshWindowMinutes);
        if (now.isBefore(refreshWindowStart)) {
            throw new TokenException(TokenException.ErrorCode.TOKEN_OUTSIDE_REFRESH_WINDOW);
        }

        String newToken = generateToken(oldTokenInfo.getUserId(), defaultExpireMinutes);

        tokenStore.remove(oldToken);

        return newToken;
    }

    public boolean isWithinRefreshWindow(String token) {
        TokenInfo tokenInfo = tokenStore.get(token);
        if (tokenInfo == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = tokenInfo.getExpireTime();
        LocalDateTime refreshWindowStart = expireTime.minusMinutes(refreshWindowMinutes);
        return now.isAfter(refreshWindowStart) && now.isBefore(expireTime);
    }
}
