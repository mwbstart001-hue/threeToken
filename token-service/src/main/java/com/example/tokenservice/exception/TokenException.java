package com.example.tokenservice.exception;

import com.example.tokenservice.common.ResultCode;

public class TokenException extends RuntimeException {

    private final int code;

    public TokenException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public TokenException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
