package com.example.tokenservice.dto;

public class RenewTokenResponse {

    private boolean success;
    private String token;
    private String refreshToken;
    private String userId;
    private long expireTime;
    private String message;

    public RenewTokenResponse() {
    }

    public static RenewTokenResponse success(String token, String refreshToken, String userId, long expireTime) {
        RenewTokenResponse response = new RenewTokenResponse();
        response.success = true;
        response.token = token;
        response.refreshToken = refreshToken;
        response.userId = userId;
        response.expireTime = expireTime;
        return response;
    }

    public static RenewTokenResponse error(String message) {
        RenewTokenResponse response = new RenewTokenResponse();
        response.success = false;
        response.message = message;
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
