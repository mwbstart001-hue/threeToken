package com.example.tokenservice;

import com.example.tokenservice.common.ResultCode;
import com.example.tokenservice.exception.TokenException;
import com.example.tokenservice.model.TokenInfo;
import com.example.tokenservice.service.TokenService;
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

    private String validToken;

    @Before
    public void setUp() {
        TokenInfo tokenInfo = tokenService.generateToken("testUser");
        validToken = tokenInfo.getToken();
    }

    @Test
    public void generateToken_Success() {
        TokenInfo tokenInfo = tokenService.generateToken("user123");
        assertNotNull(tokenInfo);
        assertNotNull(tokenInfo.getToken());
        assertEquals("user123", tokenInfo.getUserId());
        assertTrue(tokenInfo.isValid());
    }

    @Test(expected = TokenException.class)
    public void generateToken_EmptyUserId_ShouldThrowException() {
        try {
            tokenService.generateToken("");
        } catch (TokenException e) {
            assertEquals(ResultCode.USER_ID_EMPTY.getCode(), e.getCode());
            throw e;
        }
    }

    @Test(expected = TokenException.class)
    public void generateToken_NullUserId_ShouldThrowException() {
        try {
            tokenService.generateToken(null);
        } catch (TokenException e) {
            assertEquals(ResultCode.USER_ID_EMPTY.getCode(), e.getCode());
            throw e;
        }
    }

    @Test
    public void validateToken_Success() {
        TokenInfo tokenInfo = tokenService.validateToken(validToken);
        assertNotNull(tokenInfo);
        assertEquals("testUser", tokenInfo.getUserId());
        assertTrue(tokenInfo.isValid());
    }

    @Test(expected = TokenException.class)
    public void validateToken_EmptyToken_ShouldThrowException() {
        try {
            tokenService.validateToken("");
        } catch (TokenException e) {
            assertEquals(ResultCode.TOKEN_EMPTY.getCode(), e.getCode());
            throw e;
        }
    }

    @Test(expected = TokenException.class)
    public void validateToken_NullToken_ShouldThrowException() {
        try {
            tokenService.validateToken(null);
        } catch (TokenException e) {
            assertEquals(ResultCode.TOKEN_EMPTY.getCode(), e.getCode());
            throw e;
        }
    }

    @Test(expected = TokenException.class)
    public void validateToken_InvalidToken_ShouldThrowException() {
        try {
            tokenService.validateToken("invalidToken123");
        } catch (TokenException e) {
            assertEquals(ResultCode.TOKEN_INVALID.getCode(), e.getCode());
            throw e;
        }
    }

    @Test(expected = TokenException.class)
    public void validateToken_InvalidatedToken_ShouldThrowException() {
        tokenService.invalidateToken(validToken);
        try {
            tokenService.validateToken(validToken);
        } catch (TokenException e) {
            assertEquals(ResultCode.TOKEN_INVALID.getCode(), e.getCode());
            throw e;
        }
    }

    @Test
    public void invalidateToken_Success() {
        tokenService.invalidateToken(validToken);
    }

    @Test(expected = TokenException.class)
    public void invalidateToken_EmptyToken_ShouldThrowException() {
        try {
            tokenService.invalidateToken("");
        } catch (TokenException e) {
            assertEquals(ResultCode.TOKEN_EMPTY.getCode(), e.getCode());
            throw e;
        }
    }

    @Test(expected = TokenException.class)
    public void invalidateToken_InvalidToken_ShouldThrowException() {
        try {
            tokenService.invalidateToken("nonexistent");
        } catch (TokenException e) {
            assertEquals(ResultCode.TOKEN_INVALID.getCode(), e.getCode());
            throw e;
        }
    }
}
