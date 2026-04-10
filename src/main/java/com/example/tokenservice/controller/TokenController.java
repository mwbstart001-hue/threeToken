package com.example.tokenservice.controller;

import com.example.tokenservice.model.TokenInfo;
import com.example.tokenservice.service.SecureTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/token")
public class TokenController {

    @Autowired
    private SecureTokenService tokenService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateToken(@RequestParam String userId) {
        Map<String, Object> result = new HashMap<>();

        if (userId == null || userId.isEmpty()) {
            result.put("success", false);
            result.put("message", "userId cannot be empty");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            TokenInfo tokenInfo = tokenService.generateToken(userId);

            result.put("success", true);
            result.put("token", tokenInfo.getToken());
            result.put("refreshToken", tokenInfo.getRefreshToken());
            result.put("userId", tokenInfo.getUserId());
            result.put("expireTime", tokenInfo.getExpireTime());

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestParam String refreshToken) {
        Map<String, Object> result = new HashMap<>();

        if (refreshToken == null || refreshToken.isEmpty()) {
            result.put("success", false);
            result.put("message", "refreshToken cannot be empty");
            return ResponseEntity.badRequest().body(result);
        }

        TokenInfo tokenInfo = tokenService.refreshToken(refreshToken);

        if (tokenInfo != null) {
            result.put("success", true);
            result.put("token", tokenInfo.getToken());
            result.put("refreshToken", tokenInfo.getRefreshToken());
            result.put("userId", tokenInfo.getUserId());
            result.put("expireTime", tokenInfo.getExpireTime());
            return ResponseEntity.ok(result);
        } else {
            result.put("success", false);
            result.put("message", "refresh token is invalid or expired");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam String token) {
        Map<String, Object> result = new HashMap<>();

        if (token == null || token.isEmpty()) {
            result.put("valid", false);
            result.put("message", "token cannot be empty");
            return ResponseEntity.badRequest().body(result);
        }

        TokenInfo tokenInfo = tokenService.validateToken(token);

        if (tokenInfo != null) {
            result.put("valid", true);
            result.put("userId", tokenInfo.getUserId());
            result.put("expireTime", tokenInfo.getExpireTime());
            return ResponseEntity.ok(result);
        } else {
            result.put("valid", false);
            result.put("message", "token is invalid or expired");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
    }

    @DeleteMapping("/invalidate")
    public ResponseEntity<Map<String, Object>> invalidateToken(@RequestParam String token) {
        Map<String, Object> result = new HashMap<>();

        if (token == null || token.isEmpty()) {
            result.put("success", false);
            result.put("message", "token cannot be empty");
            return ResponseEntity.badRequest().body(result);
        }

        boolean success = tokenService.invalidateToken(token);

        result.put("success", success);
        if (success) {
            result.put("message", "token invalidated successfully");
            return ResponseEntity.ok(result);
        } else {
            result.put("message", "token not found or already invalidated");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }
}
