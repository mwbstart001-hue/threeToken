package com.example.tokenservice.model;

import java.io.Serializable;

public class TokenInfo implements Serializable {
    
    private String token;
    private String refreshToken;
    private String userId;
    private long createTime;
    private long expireTime;

    public TokenInfo() {
    }

    public TokenInfo(String token, String refreshToken, String userId, long createTime, long expireTime) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.createTime = createTime;
        this.expireTime = expireTime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
}
