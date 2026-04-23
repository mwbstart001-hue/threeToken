package com.example.tokenservice.exception;

public class TokenException extends RuntimeException {

    private final ErrorCode errorCode;

    public TokenException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public TokenException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public enum ErrorCode {
        TOKEN_NOT_FOUND("Token 不存在"),
        TOKEN_EXPIRED("Token 已过期"),
        TOKEN_REVOKED("Token 已作废"),
        TOKEN_OUTSIDE_REFRESH_WINDOW("Token 不在续签时间窗口内"),
        TOKEN_INVALID("Token 无效");

        private final String message;

        ErrorCode(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
