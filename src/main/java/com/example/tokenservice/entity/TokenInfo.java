package com.example.tokenservice.entity;

import java.time.LocalDateTime;

public class TokenInfo {
    private String token;
    private String userId;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    private boolean revoked;

    public TokenInfo() {
    }

    public TokenInfo(String token, String userId, LocalDateTime createTime, LocalDateTime expireTime) {
        this.token = token;
        this.userId = userId;
        this.createTime = createTime;
        this.expireTime = expireTime;
        this.revoked = false;
    }

    public boolean isValid() {
        return !revoked && LocalDateTime.now().isBefore(expireTime);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}
