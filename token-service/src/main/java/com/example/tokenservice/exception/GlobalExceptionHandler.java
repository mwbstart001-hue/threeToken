package com.example.tokenservice.exception;

import com.example.tokenservice.common.ApiResult;
import com.example.tokenservice.common.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TokenException.class)
    public ApiResult<Void> handleTokenException(TokenException e, HttpServletRequest request) {
        logger.warn("Token业务异常 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ApiResult.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleMissingParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        logger.warn("请求参数缺失 [{}]: {}", request.getRequestURI(), e.getMessage());
        String message = "参数 '" + e.getParameterName() + "' 不能为空";
        return ApiResult.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        logger.warn("参数类型不匹配 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ApiResult.fail(ResultCode.PARAM_ERROR.getCode(), "参数类型错误");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleValidationException(Exception e, HttpServletRequest request) {
        logger.warn("参数校验失败 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ApiResult.fail(ResultCode.PARAM_ERROR.getCode(), "参数校验失败");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logger.warn("非法参数 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ApiResult.fail(ResultCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<Void> handleException(Exception e, HttpServletRequest request) {
        logger.error("系统异常 [{}]: ", request.getRequestURI(), e);
        return ApiResult.fail(ResultCode.FAIL.getCode(), "系统内部错误，请稍后重试");
    }
}
