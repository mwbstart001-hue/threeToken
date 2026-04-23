package com.example.tokenservice.dto;

public class TokenResponse {
    private int code;
    private String message;
    private Object data;

    public TokenResponse(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static TokenResponse success(Object data) {
        return new TokenResponse(200, "success", data);
    }

    public static TokenResponse success() {
        return new TokenResponse(200, "success", null);
    }

    public static TokenResponse error(String message) {
        return new TokenResponse(400, message, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
