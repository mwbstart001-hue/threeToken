package com.example.tokenservice.exception;

public enum ErrorCode {

    TOKEN_EMPTY("TOKEN_001", "Token不能为空"),
    TOKEN_INVALID("TOKEN_002", "Token无效或不存在"),
    TOKEN_EXPIRED("TOKEN_003", "Token已过期"),
    TOKEN_REVOKED("TOKEN_004", "Token已被作废"),
    USER_ID_EMPTY("USER_001", "用户ID不能为空"),
    SYSTEM_ERROR("SYS_001", "系统内部错误"),
    INVALID_PARAM("PARAM_001", "参数错误");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
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
