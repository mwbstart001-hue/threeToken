package com.example.tokenservice.controller;

import com.example.tokenservice.model.TokenInfo;
import com.example.tokenservice.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/token")
public class TokenController {

    @Autowired
    private TokenService tokenService;

    @PostMapping("/generate")
    public Map<String, Object> generateToken(@RequestParam String userId) {
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null || userId.isEmpty()) {
            result.put("success", false);
            result.put("message", "userId cannot be empty");
            return result;
        }
        
        TokenInfo tokenInfo = tokenService.generateToken(userId);
        
        result.put("success", true);
        result.put("token", tokenInfo.getToken());
        result.put("userId", tokenInfo.getUserId());
        result.put("createTime", tokenInfo.getCreateTime());
        result.put("expireTime", tokenInfo.getExpireTime());
        
        return result;
    }

    @GetMapping("/validate")
    public Map<String, Object> validateToken(@RequestParam String token) {
        Map<String, Object> result = new HashMap<>();
        
        if (token == null || token.isEmpty()) {
            result.put("valid", false);
            result.put("message", "token cannot be empty");
            return result;
        }
        
        TokenInfo tokenInfo = tokenService.validateToken(token);
        
        if (tokenInfo != null) {
            result.put("valid", true);
            result.put("userId", tokenInfo.getUserId());
            result.put("expireTime", tokenInfo.getExpireTime());
        } else {
            result.put("valid", false);
            result.put("message", "token is invalid or expired");
        }
        
        return result;
    }

    @DeleteMapping("/invalidate")
    public Map<String, Object> invalidateToken(@RequestParam String token) {
        Map<String, Object> result = new HashMap<>();
        
        if (token == null || token.isEmpty()) {
            result.put("success", false);
            result.put("message", "token cannot be empty");
            return result;
        }
        
        boolean success = tokenService.invalidateToken(token);
        
        result.put("success", success);
        if (success) {
            result.put("message", "token invalidated successfully");
        } else {
            result.put("message", "token not found or already invalidated");
        }
        
        return result;
    }
}
