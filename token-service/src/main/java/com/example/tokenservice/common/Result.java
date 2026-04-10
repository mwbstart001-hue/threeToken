package com.example.tokenservice.common;

import java.io.Serializable;

public class Result<T> implements Serializable {
    
    private Integer code;
    private String message;
    private T data;
    private Boolean success;

    public Result() {
    }

    public Result(Integer code, String message, T data, Boolean success) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "success", null, true);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data, true);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data, true);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null, false);
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null, false);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
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

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
