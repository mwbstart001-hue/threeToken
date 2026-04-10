package com.example.tokenservice;

import com.example.tokenservice.config.JwtConfig;
import com.example.tokenservice.model.TokenInfo;
import com.example.tokenservice.service.TokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TokenSecurityTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JwtConfig jwtConfig;

    @Test
    public void testTokenGeneration_ContainsSignature() {
        TokenInfo tokenInfo = tokenService.generateToken("user123");
        String token = tokenInfo.getToken();

        System.out.println("Generated Token: " + token);

        String[] parts = token.split("\\.");
        assertEquals("JWT should have 3 parts (header.payload.signature)", 3, parts.length);

        assertNotNull("Signature should not be empty", parts[2]);
        assertTrue("Signature length should be adequate", parts[2].length() > 10);
    }

    @Test
    public void testTokenValidation_SignatureTampering() {
        TokenInfo tokenInfo = tokenService.generateToken("user123");
        String originalToken = tokenInfo.getToken();

        String[] parts = originalToken.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + "." + parts[2] + "x";

        TokenInfo result = tokenService.validateToken(tamperedToken);
        assertNull("Tampered token should be rejected", result);
    }

    @Test
    public void testTokenValidation_PayloadTampering() {
        TokenInfo tokenInfo = tokenService.generateToken("user123");
        String originalToken = tokenInfo.getToken();

        String[] parts = originalToken.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

        String payload = new String(decoder.decode(parts[1]));
        String tamperedPayload = payload.replace("\"sub\":\"user123\"", "\"sub\":\"admin\"");
        String tamperedPayloadEncoded = encoder.encodeToString(tamperedPayload.getBytes());

        String tamperedToken = parts[0] + "." + tamperedPayloadEncoded + "." + parts[2];

        TokenInfo result = tokenService.validateToken(tamperedToken);
        assertNull("Token with tampered payload should be rejected", result);
    }

    @Test
    public void testTokenValidation_WrongKeySigned() {
        SecretKey fakeKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        String fakeToken = Jwts.builder()
                .setSubject("user123")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(fakeKey, SignatureAlgorithm.HS256)
                .compact();

        TokenInfo result = tokenService.validateToken(fakeToken);
        assertNull("Token signed with wrong key should be rejected", result);
    }

    @Test
    public void testTokenInvalidation_Blacklist() {
        TokenInfo tokenInfo = tokenService.generateToken("user123");
        String token = tokenInfo.getToken();

        TokenInfo validBefore = tokenService.validateToken(token);
        assertNotNull("Token should be valid before invalidation", validBefore);

        boolean invalidated = tokenService.invalidateToken(token);
        assertTrue("Invalidation should succeed", invalidated);

        TokenInfo validAfter = tokenService.validateToken(token);
        assertNull("Token should be invalid after invalidation", validAfter);
    }

    @Test
    public void testTokenUniqueness() {
        Set<String> tokens = new HashSet<>();
        int iterations = 1000;

        for (int i = 0; i < iterations; i++) {
            TokenInfo tokenInfo = tokenService.generateToken("user" + i);
            String token = tokenInfo.getToken();

            assertFalse("Duplicate token found!", tokens.contains(token));
            tokens.add(token);
        }

        assertEquals(iterations, tokens.size());
    }

    @Test
    public void testTokenEntropyAnalysis() {
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            TokenInfo tokenInfo = tokenService.generateToken("user" + i);
            tokens.add(tokenInfo.getToken());
        }

        double totalHammingDistance = 0;
        int comparisons = 0;

        for (int i = 0; i < tokens.size(); i++) {
            for (int j = i + 1; j < tokens.size(); j++) {
                totalHammingDistance += calculateHammingDistance(tokens.get(i), tokens.get(j));
                comparisons++;
            }
        }

        double avgHammingDistance = totalHammingDistance / comparisons;
        System.out.printf("Average Hamming Distance between tokens: %.2f%%\n",
                (avgHammingDistance / tokens.get(0).length()) * 100);

        assertTrue("Average Hamming distance should be > 20%",
                (avgHammingDistance / tokens.get(0).length()) > 0.20);
    }

    @Test
    public void testNoSecretLeakage() throws Exception {
        Field field = JwtConfig.class.getDeclaredField("secret");
        field.setAccessible(true);
        String secret = (String) field.get(jwtConfig);

        for (int i = 0; i < 10; i++) {
            TokenInfo tokenInfo = tokenService.generateToken("user" + i);
            String token = tokenInfo.getToken();

            assertFalse("Secret should not be present in token",
                    token.contains(secret.substring(0, Math.min(10, secret.length()))));
        }
    }

    @Test
    public void testExpiredTokenRejection() throws Exception {
        String token = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject("user123")
                .setIssuedAt(new Date(System.currentTimeMillis() - 5000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(jwtConfig.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        TokenInfo result = tokenService.validateToken(token);
        assertNull("Expired token should be rejected", result);
    }

    @Test
    public void testSecureKeyGeneration() {
        String generatedSecret = JwtConfig.generateSecureSecret();
        System.out.println("Generated secure secret: " + generatedSecret);

        byte[] decoded = Base64.getDecoder().decode(generatedSecret);
        int keyBitLength = decoded.length * 8;
        System.out.println("Key bit length: " + keyBitLength);

        assertEquals("HS256 key should be 256 bits", 256, keyBitLength);
    }

    private int calculateHammingDistance(String s1, String s2) {
        int distance = 0;
        int minLength = Math.min(s1.length(), s2.length());

        for (int i = 0; i < minLength; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                distance++;
            }
        }

        distance += Math.abs(s1.length() - s2.length());
        return distance;
    }
}
