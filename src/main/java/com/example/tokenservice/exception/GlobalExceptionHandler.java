package com.example.tokenservice.exception;

import com.example.tokenservice.dto.TokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<TokenResponse> handleTokenException(TokenException ex) {
        TokenException.ErrorCode errorCode = ex.getErrorCode();
        HttpStatus httpStatus = mapToHttpStatus(errorCode);
        TokenResponse response = TokenResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, httpStatus);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<TokenResponse> handleGeneralException(Exception ex) {
        TokenResponse response = TokenResponse.error("服务器内部错误: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatus mapToHttpStatus(TokenException.ErrorCode errorCode) {
        switch (errorCode) {
            case TOKEN_NOT_FOUND:
                return HttpStatus.NOT_FOUND;
            case TOKEN_EXPIRED:
            case TOKEN_REVOKED:
            case TOKEN_INVALID:
            case TOKEN_OUTSIDE_REFRESH_WINDOW:
                return HttpStatus.BAD_REQUEST;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
