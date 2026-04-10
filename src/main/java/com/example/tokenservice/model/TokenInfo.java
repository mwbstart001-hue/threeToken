package com.example.tokenservice.model;

import java.io.Serializable;

public class TokenInfo implements Serializable {

    private String token;
    private String userId;
    private long createTime;
    private long expireTime;
    private String jti;
    private boolean valid;

    public TokenInfo() {
    }

    public TokenInfo(String token, String userId, long createTime, long expireTime) {
        this.token = token;
        this.userId = userId;
        this.createTime = createTime;
        this.expireTime = expireTime;
        this.valid = true;
    }

    public TokenInfo(String token, String userId, long createTime, long expireTime, String jti) {
        this.token = token;
        this.userId = userId;
        this.createTime = createTime;
        this.expireTime = expireTime;
        this.jti = jti;
        this.valid = true;
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

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
