package com.example.tokenservice.service;

import com.example.tokenservice.exception.TokenRenewalException;
import com.example.tokenservice.model.TokenInfo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.Assert.*;

public class SecureTokenServiceTest {

    private SecureTokenService tokenService;

    @Before
    public void setUp() {
        tokenService = new SecureTokenService();
        ReflectionTestUtils.setField(tokenService, "jwtSecret", "test-secret-key-for-jwt-signing-that-is-long-enough-for-hs512-algorithm-xyz123");
        ReflectionTestUtils.setField(tokenService, "expirationTime", 3600000L);
        ReflectionTestUtils.setField(tokenService, "refreshExpirationTime", 86400000L);
        ReflectionTestUtils.setField(tokenService, "refreshWindow", 3600000L);
        tokenService.init();
    }

    @Test
    public void testGenerateToken_Success() {
        String userId = "user123";
        TokenInfo tokenInfo = tokenService.generateToken(userId);

        assertNotNull(tokenInfo);
        assertNotNull(tokenInfo.getToken());
        assertNotNull(tokenInfo.getRefreshToken());
        assertEquals(userId, tokenInfo.getUserId());
        assertTrue(tokenInfo.getExpireTime() > System.currentTimeMillis());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateToken_EmptyUserId() {
        tokenService.generateToken("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateToken_NullUserId() {
        tokenService.generateToken(null);
    }

    @Test
    public void testValidateToken_ValidToken() {
        String userId = "user123";
        TokenInfo tokenInfo = tokenService.generateToken(userId);

        TokenInfo validatedInfo = tokenService.validateToken(tokenInfo.getToken());

        assertNotNull(validatedInfo);
        assertEquals(userId, validatedInfo.getUserId());
    }

    @Test
    public void testValidateToken_InvalidToken() {
        TokenInfo validatedInfo = tokenService.validateToken("invalid.token.here");
        assertNull(validatedInfo);
    }

    @Test
    public void testValidateToken_NullToken() {
        TokenInfo validatedInfo = tokenService.validateToken(null);
        assertNull(validatedInfo);
    }

    @Test
    public void testValidateToken_EmptyToken() {
        TokenInfo validatedInfo = tokenService.validateToken("");
        assertNull(validatedInfo);
    }

    @Test
    public void testInvalidateToken_Success() {
        String userId = "user123";
        TokenInfo tokenInfo = tokenService.generateToken(userId);

        boolean invalidated = tokenService.invalidateToken(tokenInfo.getToken());
        assertTrue(invalidated);

        TokenInfo validatedInfo = tokenService.validateToken(tokenInfo.getToken());
        assertNull(validatedInfo);
    }

    @Test
    public void testInvalidateToken_AlreadyInvalidated() {
        String userId = "user123";
        TokenInfo tokenInfo = tokenService.generateToken(userId);

        tokenService.invalidateToken(tokenInfo.getToken());

        boolean invalidatedAgain = tokenService.invalidateToken(tokenInfo.getToken());
        assertTrue(invalidatedAgain);
    }

    @Test
    public void testInvalidateToken_InvalidToken() {
        boolean invalidated = tokenService.invalidateToken("invalid.token.here");
        assertFalse(invalidated);
    }

    @Test
    public void testRefreshToken_Success() {
        String userId = "user123";
        TokenInfo originalToken = tokenService.generateToken(userId);

        TokenInfo refreshedToken = tokenService.refreshToken(originalToken.getRefreshToken());

        assertNotNull(refreshedToken);
        assertNotNull(refreshedToken.getToken());
        assertNotNull(refreshedToken.getRefreshToken());
        assertEquals(userId, refreshedToken.getUserId());

        TokenInfo oldRefreshTokenValidated = tokenService.refreshToken(originalToken.getRefreshToken());
        assertNull(oldRefreshTokenValidated);
    }

    @Test
    public void testRefreshToken_InvalidToken() {
        TokenInfo refreshedToken = tokenService.refreshToken("invalid-refresh-token-string");
        assertNull(refreshedToken);
    }

    @Test
    public void testRefreshToken_UsingAccessToken() {
        String userId = "user123";
        TokenInfo tokenInfo = tokenService.generateToken(userId);

        TokenInfo refreshedToken = tokenService.refreshToken(tokenInfo.getToken());
        assertNull(refreshedToken);
    }

    @Test
    public void testTokenSignatureCannotBeForged() {
        String userId = "user123";
        TokenInfo tokenInfo = tokenService.generateToken(userId);
        String originalToken = tokenInfo.getToken();

        String[] parts = originalToken.split("\\.");
        assertEquals(3, parts.length);

        String tamperedPayload = parts[1].substring(0, parts[1].length() - 1) + "X";
        String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];

        TokenInfo validatedInfo = tokenService.validateToken(tamperedToken);
        assertNull("Tampered token should be rejected", validatedInfo);
    }

    @Test
    public void testTokenStructure() {
        String userId = "user123";
        TokenInfo tokenInfo = tokenService.generateToken(userId);
        String token = tokenInfo.getToken();

        String[] parts = token.split("\\.");
        assertEquals("JWT should have 3 parts", 3, parts.length);

        assertFalse("Header should not be empty", parts[0].isEmpty());
        assertFalse("Payload should not be empty", parts[1].isEmpty());
        assertFalse("Signature should not be empty", parts[2].isEmpty());
    }

    @Test
    public void testDifferentUsersGetDifferentTokens() {
        TokenInfo token1 = tokenService.generateToken("user1");
        TokenInfo token2 = tokenService.generateToken("user2");

        assertNotEquals("Different users should get different tokens",
                token1.getToken(), token2.getToken());
        assertNotEquals("Different users should get different refresh tokens",
                token1.getRefreshToken(), token2.getRefreshToken());
    }

    @Test
    public void testSameUserGetsDifferentTokensOnEachRequest() {
        String userId = "user123";
        TokenInfo token1 = tokenService.generateToken(userId);
        TokenInfo token2 = tokenService.generateToken(userId);

        assertNotEquals("Same user should get different tokens on each request",
                token1.getToken(), token2.getToken());
    }

    @Test
    public void testTokenContainsExpectedClaims() throws Exception {
        String userId = "user123";
        TokenInfo tokenInfo = tokenService.generateToken(userId);
        String token = tokenInfo.getToken();

        String[] parts = token.split("\\.");
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

        assertTrue("Payload should contain userId (sub)", payloadJson.contains("\"sub\":\"" + userId + "\""));
        assertTrue("Payload should contain type claim", payloadJson.contains("\"type\":\"access\""));
        assertTrue("Payload should contain jti", payloadJson.contains("\"jti\":"));
    }

    @Test
    public void testIsTokenRevoked() {
        String userId = "user123";
        TokenInfo tokenInfo = tokenService.generateToken(userId);

        assertFalse(tokenService.isTokenRevoked(tokenInfo.getToken()));

        tokenService.invalidateToken(tokenInfo.getToken());

        assertTrue(tokenService.isTokenRevoked(tokenInfo.getToken()));
    }

    @Test
    public void testInvalidateRefreshToken() {
        String userId = "user123";
        TokenInfo tokenInfo = tokenService.generateToken(userId);

        boolean invalidated = tokenService.invalidateToken(tokenInfo.getRefreshToken());
        assertTrue(invalidated);

        TokenInfo refreshed = tokenService.refreshToken(tokenInfo.getRefreshToken());
        assertNull(refreshed);
    }

    @Test
    public void testRenewToken_Success() {
        String userId = "user123";
        TokenInfo originalToken = tokenService.generateToken(userId);

        TokenInfo renewedToken = tokenService.renewToken(originalToken.getToken());

        assertNotNull(renewedToken);
        assertNotNull(renewedToken.getToken());
        assertNotNull(renewedToken.getRefreshToken());
        assertEquals(userId, renewedToken.getUserId());
        assertTrue(renewedToken.getExpireTime() > System.currentTimeMillis());

        TokenInfo oldTokenValidated = tokenService.validateToken(originalToken.getToken());
        assertNull("Old token should be revoked after renewal", oldTokenValidated);
    }

    @Test(expected = TokenRenewalException.class)
    public void testRenewToken_NullToken() {
        tokenService.renewToken(null);
    }

    @Test(expected = TokenRenewalException.class)
    public void testRenewToken_EmptyToken() {
        tokenService.renewToken("");
    }

    @Test(expected = TokenRenewalException.class)
    public void testRenewToken_InvalidToken() {
        tokenService.renewToken("invalid.token.here");
    }

    @Test(expected = TokenRenewalException.class)
    public void testRenewToken_UsingRefreshToken() {
        String userId = "user123";
        TokenInfo tokenInfo = tokenService.generateToken(userId);

        tokenService.renewToken(tokenInfo.getRefreshToken());
    }

    @Test(expected = TokenRenewalException.class)
    public void testRenewToken_RevokedToken() {
        String userId = "user123";
        TokenInfo tokenInfo = tokenService.generateToken(userId);

        tokenService.invalidateToken(tokenInfo.getToken());

        tokenService.renewToken(tokenInfo.getToken());
    }

    @Test(expected = TokenRenewalException.class)
    public void testRenewToken_AlreadyRenewed() {
        String userId = "user123";
        TokenInfo tokenInfo = tokenService.generateToken(userId);

        tokenService.renewToken(tokenInfo.getToken());

        tokenService.renewToken(tokenInfo.getToken());
    }
}
