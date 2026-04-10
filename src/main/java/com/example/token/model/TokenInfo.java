package com.example.token.model;

import java.util.Date;

public class TokenInfo {
    private String token;
    private String userId;
    private Date createTime;
    private Date expireTime;
    private boolean revoked;

    public TokenInfo(String token, String userId, Date createTime, Date expireTime) {
        this.token = token;
        this.userId = userId;
        this.createTime = createTime;
        this.expireTime = expireTime;
        this.revoked = false;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public boolean isExpired() {
        return new Date().after(expireTime);
    }
}
