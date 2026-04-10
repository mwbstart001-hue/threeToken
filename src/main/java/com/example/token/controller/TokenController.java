package com.example.token.controller;

import com.example.token.model.ApiResponse;
import com.example.token.model.TokenInfo;
import com.example.token.service.TokenService;
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
    public ApiResponse<Map<String, Object>> generateToken(
            @RequestParam String userId,
            @RequestParam(required = false, defaultValue = "24") long expireHours) {

        String token = tokenService.generateToken(userId, expireHours);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", userId);
        data.put("expireHours", expireHours);

        return ApiResponse.success(data);
    }

    @GetMapping("/validate")
    public ApiResponse<Map<String, Object>> validateToken(@RequestParam String token) {
        TokenInfo tokenInfo = tokenService.validateToken(token);

        if (tokenInfo == null) {
            return ApiResponse.error(401, "Token invalid or expired");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("valid", true);
        data.put("token", tokenInfo.getToken());
        data.put("userId", tokenInfo.getUserId());
        data.put("createTime", tokenInfo.getCreateTime());
        data.put("expireTime", tokenInfo.getExpireTime());

        return ApiResponse.success(data);
    }

    @PostMapping("/revoke")
    public ApiResponse<Map<String, Object>> revokeToken(@RequestParam String token) {
        boolean success = tokenService.revokeToken(token);

        if (!success) {
            return ApiResponse.error(404, "Token not found");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("revoked", true);
        data.put("token", token);

        return ApiResponse.success(data);
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getStatus() {
        Map<String, Object> data = new HashMap<>();
        data.put("tokenCount", tokenService.getTokenCount());
        data.put("status", "running");

        return ApiResponse.success(data);
    }
}
