package com.example.tokenservice.controller;

import com.example.tokenservice.dto.TokenResponse;
import com.example.tokenservice.entity.TokenInfo;
import com.example.tokenservice.service.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/token")
public class TokenController {

    @Autowired
    private TokenManager tokenManager;

    @PostMapping("/generate")
    public TokenResponse generateToken(@RequestParam String userId,
                                       @RequestParam(required = false) Integer expireMinutes) {
        String token;
        if (expireMinutes != null) {
            token = tokenManager.generateToken(userId, expireMinutes);
        } else {
            token = tokenManager.generateToken(userId);
        }
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        return TokenResponse.success(result);
    }

    @GetMapping("/validate")
    public TokenResponse validateToken(@RequestParam String token) {
        boolean valid = tokenManager.validateToken(token);
        Map<String, Object> result = new HashMap<>();
        result.put("valid", valid);
        if (valid) {
            TokenInfo tokenInfo = tokenManager.getTokenInfo(token);
            result.put("userId", tokenInfo.getUserId());
            result.put("expireTime", tokenInfo.getExpireTime().toString());
        }
        return TokenResponse.success(result);
    }

    @PostMapping("/revoke")
    public TokenResponse revokeToken(@RequestParam String token) {
        boolean revoked = tokenManager.revokeToken(token);
        if (revoked) {
            Map<String, Boolean> result = new HashMap<>();
            result.put("revoked", true);
            return TokenResponse.success(result);
        } else {
            return TokenResponse.error("Token not found");
        }
    }
}
