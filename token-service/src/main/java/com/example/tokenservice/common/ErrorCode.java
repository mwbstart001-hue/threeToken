package com.example.tokenservice.common;

public enum ErrorCode {
    
    PARAM_ERROR(400, "Parameter error"),
    TOKEN_EMPTY(1001, "Token cannot be empty"),
    TOKEN_INVALID(1002, "Token is invalid"),
    TOKEN_EXPIRED(1003, "Token has expired"),
    TOKEN_REVOKED(1004, "Token has been revoked"),
    USER_ID_EMPTY(1005, "UserId cannot be empty"),
    SYSTEM_ERROR(500, "System error");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
