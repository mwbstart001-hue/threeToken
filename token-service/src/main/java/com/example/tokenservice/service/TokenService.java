package com.example.tokenservice.service;

import com.example.tokenservice.exception.ErrorCode;
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
        if (userId == null || userId.isEmpty()) {
            throw new TokenException(ErrorCode.USER_ID_EMPTY.getCode(), ErrorCode.USER_ID_EMPTY.getMessage());
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        long createTime = System.currentTimeMillis();
        long expireTime = createTime + DEFAULT_EXPIRE_TIME;

        TokenInfo tokenInfo = new TokenInfo(token, userId, createTime, expireTime);
        tokenStore.put(token, tokenInfo);

        return tokenInfo;
    }

    public TokenInfo validateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new TokenException(ErrorCode.TOKEN_EMPTY.getCode(), ErrorCode.TOKEN_EMPTY.getMessage());
        }

        TokenInfo tokenInfo = tokenStore.get(token);

        if (tokenInfo == null) {
            throw new TokenException(ErrorCode.TOKEN_INVALID.getCode(), ErrorCode.TOKEN_INVALID.getMessage());
        }

        if (!tokenInfo.isValid()) {
            throw new TokenException(ErrorCode.TOKEN_REVOKED.getCode(), ErrorCode.TOKEN_REVOKED.getMessage());
        }

        if (System.currentTimeMillis() > tokenInfo.getExpireTime()) {
            tokenStore.remove(token);
            throw new TokenException(ErrorCode.TOKEN_EXPIRED.getCode(), ErrorCode.TOKEN_EXPIRED.getMessage());
        }

        return tokenInfo;
    }

    public boolean invalidateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new TokenException(ErrorCode.TOKEN_EMPTY.getCode(), ErrorCode.TOKEN_EMPTY.getMessage());
        }

        TokenInfo tokenInfo = tokenStore.get(token);

        if (tokenInfo == null) {
            throw new TokenException(ErrorCode.TOKEN_INVALID.getCode(), ErrorCode.TOKEN_INVALID.getMessage());
        }

        tokenInfo.setValid(false);
        tokenStore.remove(token);

        return true;
    }
}
