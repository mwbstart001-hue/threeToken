package com.example.tokenservice.model;

import java.io.Serializable;

public class TokenInfo implements Serializable {
    
    private String token;
    private String userId;
    private long createTime;
    private long expireTime;
    private boolean valid;
    private String deviceId;

    public TokenInfo() {
    }

    public TokenInfo(String token, String userId, long createTime, long expireTime) {
        this.token = token;
        this.userId = userId;
        this.createTime = createTime;
        this.expireTime = expireTime;
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

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
