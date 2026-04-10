package com.example.tokenservice.exception;

import com.example.tokenservice.common.ErrorCode;
import com.example.tokenservice.common.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GlobalExceptionHandlerTest {

    @Test
    public void testBusinessException() {
        BusinessException exception = new BusinessException(ErrorCode.TOKEN_EMPTY);
        
        assertEquals(ErrorCode.TOKEN_EMPTY.getCode(), exception.getCode());
        assertEquals(ErrorCode.TOKEN_EMPTY.getMessage(), exception.getMessage());
    }

    @Test
    public void testBusinessException_WithCustomMessage() {
        BusinessException exception = new BusinessException(ErrorCode.TOKEN_INVALID, "Custom message");
        
        assertEquals(ErrorCode.TOKEN_INVALID.getCode(), exception.getCode());
        assertEquals("Custom message", exception.getMessage());
    }

    @Test
    public void testErrorCode() {
        assertEquals(Integer.valueOf(1001), ErrorCode.TOKEN_EMPTY.getCode());
        assertEquals("Token cannot be empty", ErrorCode.TOKEN_EMPTY.getMessage());
        
        assertEquals(Integer.valueOf(1002), ErrorCode.TOKEN_INVALID.getCode());
        assertEquals("Token is invalid", ErrorCode.TOKEN_INVALID.getMessage());
        
        assertEquals(Integer.valueOf(1003), ErrorCode.TOKEN_EXPIRED.getCode());
        assertEquals("Token has expired", ErrorCode.TOKEN_EXPIRED.getMessage());
        
        assertEquals(Integer.valueOf(1004), ErrorCode.TOKEN_REVOKED.getCode());
        assertEquals("Token has been revoked", ErrorCode.TOKEN_REVOKED.getMessage());
        
        assertEquals(Integer.valueOf(1005), ErrorCode.USER_ID_EMPTY.getCode());
        assertEquals("UserId cannot be empty", ErrorCode.USER_ID_EMPTY.getMessage());
    }

    @Test
    public void testResult_Success() {
        Result<String> result = Result.success("test data");
        
        assertEquals(Integer.valueOf(200), result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("test data", result.getData());
        assertTrue(result.getSuccess());
    }

    @Test
    public void testResult_Error() {
        Result<Void> result = Result.error(ErrorCode.TOKEN_EMPTY);
        
        assertEquals(ErrorCode.TOKEN_EMPTY.getCode(), result.getCode());
        assertEquals(ErrorCode.TOKEN_EMPTY.getMessage(), result.getMessage());
        assertNull(result.getData());
        assertFalse(result.getSuccess());
    }

    @Test
    public void testResult_ErrorWithCodeAndMessage() {
        Result<Void> result = Result.error(999, "Custom error");
        
        assertEquals(Integer.valueOf(999), result.getCode());
        assertEquals("Custom error", result.getMessage());
        assertNull(result.getData());
        assertFalse(result.getSuccess());
    }
}
