package com.example.tokenservice.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResult<T> implements Serializable {

    private int code;
    private String message;
    private T data;

    public ApiResult() {
    }

    public ApiResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResult<T> success() {
        return new ApiResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    public static <T> ApiResult<T> fail() {
        return new ApiResult<>(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMessage(), null);
    }

    public static <T> ApiResult<T> fail(String message) {
        return new ApiResult<>(ResultCode.FAIL.getCode(), message, null);
    }

    public static <T> ApiResult<T> fail(ResultCode resultCode) {
        return new ApiResult<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> ApiResult<T> fail(int code, String message) {
        return new ApiResult<>(code, message, null);
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
