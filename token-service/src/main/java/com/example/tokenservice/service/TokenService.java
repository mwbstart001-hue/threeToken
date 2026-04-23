package com.example.tokenservice.service;

import com.example.tokenservice.model.TokenInfo;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();

    private static final long DEFAULT_EXPIRE_TIME = 3600000;

    public TokenInfo generateToken(String userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        long createTime = System.currentTimeMillis();
        long expireTime = createTime + DEFAULT_EXPIRE_TIME;
        
        TokenInfo tokenInfo = new TokenInfo(token, userId, createTime, expireTime);
        tokenStore.put(token, tokenInfo);
        
        return tokenInfo;
    }

    public TokenInfo validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        TokenInfo tokenInfo = tokenStore.get(token);
        
        if (tokenInfo == null) {
            return null;
        }
        
        if (!tokenInfo.isValid()) {
            return null;
        }
        
        if (System.currentTimeMillis() > tokenInfo.getExpireTime()) {
            tokenStore.remove(token);
            return null;
        }
        
        return tokenInfo;
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
        
        return true;
    }
}
