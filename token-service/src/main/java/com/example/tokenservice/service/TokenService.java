package com.example.tokenservice.service;

import com.example.tokenservice.common.ResultCode;
import com.example.tokenservice.exception.TokenException;
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
        if (userId == null || userId.trim().isEmpty()) {
            throw new TokenException(ResultCode.USER_ID_EMPTY);
        }
        
        String token = UUID.randomUUID().toString().replace("-", "");
        long createTime = System.currentTimeMillis();
        long expireTime = createTime + DEFAULT_EXPIRE_TIME;
        
        TokenInfo tokenInfo = new TokenInfo(token, userId, createTime, expireTime);
        tokenStore.put(token, tokenInfo);
        
        return tokenInfo;
    }

    public TokenInfo validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new TokenException(ResultCode.TOKEN_EMPTY);
        }
        
        TokenInfo tokenInfo = tokenStore.get(token);
        
        if (tokenInfo == null) {
            throw new TokenException(ResultCode.TOKEN_INVALID);
        }
        
        if (!tokenInfo.isValid()) {
            throw new TokenException(ResultCode.TOKEN_INVALIDATED);
        }
        
        if (System.currentTimeMillis() > tokenInfo.getExpireTime()) {
            tokenStore.remove(token);
            throw new TokenException(ResultCode.TOKEN_EXPIRED);
        }
        
        return tokenInfo;
    }

    public void invalidateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new TokenException(ResultCode.TOKEN_EMPTY);
        }
        
        TokenInfo tokenInfo = tokenStore.get(token);
        
        if (tokenInfo == null) {
            throw new TokenException(ResultCode.TOKEN_INVALID);
        }
        
        if (!tokenInfo.isValid()) {
            throw new TokenException(ResultCode.TOKEN_INVALIDATED);
        }
        
        tokenInfo.setValid(false);
        tokenStore.remove(token);
    }
}
