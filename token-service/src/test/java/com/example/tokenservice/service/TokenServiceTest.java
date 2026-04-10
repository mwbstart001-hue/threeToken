package com.example.tokenservice.service;

import com.example.tokenservice.common.ErrorCode;
import com.example.tokenservice.exception.BusinessException;
import com.example.tokenservice.model.TokenInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    @Test
    public void testGenerateToken_Success() {
        TokenInfo tokenInfo = tokenService.generateToken("user123");
        
        assertNotNull(tokenInfo);
        assertNotNull(tokenInfo.getToken());
        assertEquals("user123", tokenInfo.getUserId());
        assertTrue(tokenInfo.isValid());
    }

    @Test(expected = BusinessException.class)
    public void testGenerateToken_UserIdEmpty() {
        try {
            tokenService.generateToken("");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.USER_ID_EMPTY.getCode(), e.getCode());
            throw e;
        }
    }

    @Test(expected = BusinessException.class)
    public void testGenerateToken_UserIdNull() {
        try {
            tokenService.generateToken(null);
        } catch (BusinessException e) {
            assertEquals(ErrorCode.USER_ID_EMPTY.getCode(), e.getCode());
            throw e;
        }
    }

    @Test
    public void testValidateToken_Success() {
        TokenInfo generated = tokenService.generateToken("user456");
        TokenInfo validated = tokenService.validateToken(generated.getToken());
        
        assertNotNull(validated);
        assertEquals(generated.getToken(), validated.getToken());
        assertEquals("user456", validated.getUserId());
    }

    @Test(expected = BusinessException.class)
    public void testValidateToken_Empty() {
        try {
            tokenService.validateToken("");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.TOKEN_EMPTY.getCode(), e.getCode());
            throw e;
        }
    }

    @Test(expected = BusinessException.class)
    public void testValidateToken_Null() {
        try {
            tokenService.validateToken(null);
        } catch (BusinessException e) {
            assertEquals(ErrorCode.TOKEN_EMPTY.getCode(), e.getCode());
            throw e;
        }
    }

    @Test(expected = BusinessException.class)
    public void testValidateToken_Invalid() {
        try {
            tokenService.validateToken("invalid-token-12345");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.TOKEN_INVALID.getCode(), e.getCode());
            throw e;
        }
    }

    @Test(expected = BusinessException.class)
    public void testValidateToken_Revoked() {
        TokenInfo generated = tokenService.generateToken("user789");
        String token = generated.getToken();
        
        tokenService.invalidateToken(token);
        
        try {
            tokenService.validateToken(token);
        } catch (BusinessException e) {
            assertEquals(ErrorCode.TOKEN_INVALID.getCode(), e.getCode());
            throw e;
        }
    }

    @Test
    public void testInvalidateToken_Success() {
        TokenInfo generated = tokenService.generateToken("user999");
        String token = generated.getToken();
        
        tokenService.invalidateToken(token);
        
        assertTrue(true);
    }

    @Test(expected = BusinessException.class)
    public void testInvalidateToken_Empty() {
        try {
            tokenService.invalidateToken("");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.TOKEN_EMPTY.getCode(), e.getCode());
            throw e;
        }
    }

    @Test(expected = BusinessException.class)
    public void testInvalidateToken_Null() {
        try {
            tokenService.invalidateToken(null);
        } catch (BusinessException e) {
            assertEquals(ErrorCode.TOKEN_EMPTY.getCode(), e.getCode());
            throw e;
        }
    }

    @Test(expected = BusinessException.class)
    public void testInvalidateToken_Invalid() {
        try {
            tokenService.invalidateToken("invalid-token-99999");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.TOKEN_INVALID.getCode(), e.getCode());
            throw e;
        }
    }

    @Test(expected = BusinessException.class)
    public void testInvalidateToken_AlreadyRevoked() {
        TokenInfo generated = tokenService.generateToken("user888");
        String token = generated.getToken();
        
        tokenService.invalidateToken(token);
        
        try {
            tokenService.invalidateToken(token);
        } catch (BusinessException e) {
            assertEquals(ErrorCode.TOKEN_INVALID.getCode(), e.getCode());
            throw e;
        }
    }
}
