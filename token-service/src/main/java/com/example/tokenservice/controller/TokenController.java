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
        TokenInfo tokenInfo = tokenService.generateToken(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("token", tokenInfo.getToken());
        result.put("userId", tokenInfo.getUserId());
        result.put("createTime", tokenInfo.getCreateTime());
        result.put("expireTime", tokenInfo.getExpireTime());

        return result;
    }

    @GetMapping("/validate")
    public Map<String, Object> validateToken(@RequestParam String token) {
        TokenInfo tokenInfo = tokenService.validateToken(token);

        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("userId", tokenInfo.getUserId());
        result.put("expireTime", tokenInfo.getExpireTime());

        return result;
    }

    @DeleteMapping("/invalidate")
    public Map<String, Object> invalidateToken(@RequestParam String token) {
        boolean success = tokenService.invalidateToken(token);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", "token invalidated successfully");

        return result;
    }
}
