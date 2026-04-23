package com.example.tokenservice.exception;

public class TokenRenewalException extends RuntimeException {

    private final RenewalErrorCode errorCode;

    public enum RenewalErrorCode {
        TOKEN_EXPIRED("TOKEN_EXPIRED", "Token has already expired"),
        TOKEN_REVOKED("TOKEN_REVOKED", "Token has been revoked"),
        NOT_IN_RENEWAL_WINDOW("NOT_IN_RENEWAL_WINDOW", "Token is not in renewal window"),
        INVALID_TOKEN("INVALID_TOKEN", "Token is invalid");

        private final String code;
        private final String message;

        RenewalErrorCode(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    public TokenRenewalException(RenewalErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public TokenRenewalException(RenewalErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public RenewalErrorCode getErrorCode() {
        return errorCode;
    }
}
