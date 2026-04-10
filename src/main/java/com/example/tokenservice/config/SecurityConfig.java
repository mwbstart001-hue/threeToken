package com.example.tokenservice.config;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Base64;

@Configuration
@ConfigurationProperties(prefix = "token.security")
public class SecurityConfig {

    private String secretKey;
    private long expireTime = 3600000;
    private String issuer = "token-service";
    
    private Key signingKey;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isEmpty()) {
            this.signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            this.secretKey = Base64.getEncoder().encodeToString(signingKey.getEncoded());
        } else {
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        }
    }

    public Key getSigningKey() {
        return signingKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
