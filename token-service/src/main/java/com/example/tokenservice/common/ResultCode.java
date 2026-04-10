package com.example.tokenservice.common;

public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    TOKEN_EMPTY(1001, "Token 不能为空"),
    TOKEN_INVALID(1002, "Token 非法或不存在"),
    TOKEN_EXPIRED(1003, "Token 已过期"),
    TOKEN_INVALIDATED(1004, "Token 已作废"),
    USER_ID_EMPTY(1005, "userId 不能为空");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
