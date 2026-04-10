package com.example.tokenservice;

import com.example.tokenservice.model.TokenInfo;
import com.example.tokenservice.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TokenServiceSecurityTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private com.example.tokenservice.config.SecurityConfig securityConfig;

    @Test
    public void testTokenGeneration() {
        String userId = "testUser123";
        TokenInfo tokenInfo = tokenService.generateToken(userId);
        
        assertNotNull("Token should not be null", tokenInfo.getToken());
        assertNotNull("Token should have three parts (header.payload.signature)", 
            tokenInfo.getToken().split("\\."));
        assertEquals("Token should have 3 parts", 
            3, tokenInfo.getToken().split("\\.").length);
        assertEquals("UserId should match", userId, tokenInfo.getUserId());
        assertTrue("Token should be valid", tokenInfo.isValid());
    }

    @Test
    public void testTokenValidation() {
        String userId = "testUser456";
        TokenInfo tokenInfo = tokenService.generateToken(userId);
        
        TokenInfo validatedInfo = tokenService.validateToken(tokenInfo.getToken());
        
        assertNotNull("Valid token should pass validation", validatedInfo);
        assertEquals("UserId should match after validation", userId, validatedInfo.getUserId());
    }

    @Test
    public void testInvalidToken() {
        String invalidToken = "invalid.token.string";
        
        TokenInfo result = tokenService.validateToken(invalidToken);
        
        assertNull("Invalid token should fail validation", result);
    }

    @Test
    public void testTokenWithDeviceId() {
        String userId = "testUser789";
        String deviceId = "device-12345";
        
        TokenInfo tokenInfo = tokenService.generateToken(userId, deviceId);
        
        assertNotNull("Token should be generated", tokenInfo);
        
        TokenInfo validatedWithDevice = tokenService.validateToken(tokenInfo.getToken(), deviceId);
        assertNotNull("Token should be valid with correct device ID", validatedWithDevice);
        
        TokenInfo validatedWithWrongDevice = tokenService.validateToken(tokenInfo.getToken(), "wrong-device");
        assertNull("Token should be invalid with wrong device ID", validatedWithWrongDevice);
    }

    @Test
    public void testTokenInvalidation() {
        String userId = "testUser999";
        TokenInfo tokenInfo = tokenService.generateToken(userId);
        
        boolean invalidated = tokenService.invalidateToken(tokenInfo.getToken());
        assertTrue("Token should be invalidated successfully", invalidated);
        
        TokenInfo validatedInfo = tokenService.validateToken(tokenInfo.getToken());
        assertNull("Invalidated token should fail validation", validatedInfo);
        
        boolean invalidateAgain = tokenService.invalidateToken(tokenInfo.getToken());
        assertFalse("Second invalidation should fail", invalidateAgain);
    }

    @Test
    public void testTokenSignatureVerification() {
        String userId = "testUserSignature";
        TokenInfo tokenInfo = tokenService.generateToken(userId);
        
        String[] parts = tokenInfo.getToken().split("\\.");
        assertEquals("JWT should have 3 parts", 3, parts.length);
        
        String header = new String(Base64.getUrlDecoder().decode(parts[0]));
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        
        assertTrue("Header should contain HS512 algorithm", header.contains("HS512"));
        assertTrue("Payload should contain userId", payload.contains(userId));
    }

    @Test
    public void testTokenTamperDetection() {
        String userId = "testUserTamper";
        TokenInfo tokenInfo = tokenService.generateToken(userId);
        
        String[] parts = tokenInfo.getToken().split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".tampered_signature";
        
        TokenInfo result = tokenService.validateToken(tamperedToken);
        assertNull("Tampered token should fail validation", result);
    }

    @Test
    public void testTokenPayloadTampering() {
        String userId = "testUserPayload";
        TokenInfo tokenInfo = tokenService.generateToken(userId);
        
        String[] parts = tokenInfo.getToken().split("\\.");
        
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        String tamperedPayload = payload.replace(userId, "hacker");
        String tamperedPayloadEncoded = Base64.getUrlEncoder().encodeToString(tamperedPayload.getBytes());
        
        String tamperedToken = parts[0] + "." + tamperedPayloadEncoded + "." + parts[2];
        
        TokenInfo result = tokenService.validateToken(tamperedToken);
        assertNull("Payload-tampered token should fail validation", result);
    }

    @Test
    public void testEmptyUserId() {
        TokenInfo tokenInfo = tokenService.generateToken("");
        assertNotNull("Token should be generated even with empty userId", tokenInfo);
    }

    @Test
    public void testNullUserId() {
        TokenInfo tokenInfo = tokenService.generateToken(null);
        assertNotNull("Token should be generated even with null userId", tokenInfo);
    }

    @Test
    public void testMultipleTokensForSameUser() throws InterruptedException {
        String userId = "testUserMultiple";
        
        TokenInfo token1 = tokenService.generateToken(userId);
        Thread.sleep(10);
        TokenInfo token2 = tokenService.generateToken(userId);
        
        assertNotEquals("Two tokens for same user should be different", 
            token1.getToken(), token2.getToken());
        
        assertNotNull("First token should be valid", 
            tokenService.validateToken(token1.getToken()));
        assertNotNull("Second token should be valid", 
            tokenService.validateToken(token2.getToken()));
    }

    @Test
    public void testTokenExpiration() throws InterruptedException {
        String userId = "testUserExpire";
        TokenInfo tokenInfo = tokenService.generateToken(userId);
        
        long expectedExpireTime = System.currentTimeMillis() + securityConfig.getExpireTime();
        assertTrue("Expiration time should be in the future", 
            tokenInfo.getExpireTime() > System.currentTimeMillis());
        assertTrue("Expiration time should be approximately correct", 
            Math.abs(tokenInfo.getExpireTime() - expectedExpireTime) < 1000);
    }

    @Test
    public void testJwtClaims() {
        String userId = "testUserClaims";
        String deviceId = "device-claims";
        TokenInfo tokenInfo = tokenService.generateToken(userId, deviceId);
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(securityConfig.getSigningKey())
                .build()
                .parseClaimsJws(tokenInfo.getToken())
                .getBody();
        
        assertEquals("Subject should be userId", userId, claims.getSubject());
        assertEquals("Issuer should match", securityConfig.getIssuer(), claims.getIssuer());
        assertEquals("DeviceId should match", deviceId, claims.get("deviceId", String.class));
        assertNotNull("IssuedAt should not be null", claims.getIssuedAt());
        assertNotNull("Expiration should not be null", claims.getExpiration());
    }

    @Test
    public void testTokenUniqueness() {
        int tokenCount = 100;
        Map<String, Boolean> tokenMap = new HashMap<>();
        
        for (int i = 0; i < tokenCount; i++) {
            TokenInfo tokenInfo = tokenService.generateToken("user" + i);
            String token = tokenInfo.getToken();
            
            assertFalse("Token should be unique", tokenMap.containsKey(token));
            tokenMap.put(token, true);
        }
        
        assertEquals("All tokens should be unique", tokenCount, tokenMap.size());
    }

    @Test
    public void testActiveTokenCount() {
        tokenService.generateToken("user1");
        int count1 = tokenService.getActiveTokenCount();
        
        tokenService.generateToken("user2");
        int count2 = tokenService.getActiveTokenCount();
        
        tokenService.generateToken("user3");
        int count3 = tokenService.getActiveTokenCount();
        
        assertEquals("Active token count should increase by 1 each time", 
            count1 + 2, count3);
    }
}
