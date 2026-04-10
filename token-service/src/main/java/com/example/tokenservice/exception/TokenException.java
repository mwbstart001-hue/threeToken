package com.example.tokenservice.exception;

public class TokenException extends RuntimeException {

    private final String errorCode;

    public TokenException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public TokenException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
