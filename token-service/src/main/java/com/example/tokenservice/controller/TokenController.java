package com.example.tokenservice.controller;

import com.example.tokenservice.common.ApiResult;
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
    public ApiResult<Map<String, Object>> generateToken(@RequestParam String userId) {
        TokenInfo tokenInfo = tokenService.generateToken(userId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", tokenInfo.getToken());
        data.put("userId", tokenInfo.getUserId());
        data.put("createTime", tokenInfo.getCreateTime());
        data.put("expireTime", tokenInfo.getExpireTime());
        
        return ApiResult.success(data);
    }

    @GetMapping("/validate")
    public ApiResult<Map<String, Object>> validateToken(@RequestParam String token) {
        TokenInfo tokenInfo = tokenService.validateToken(token);
        
        Map<String, Object> data = new HashMap<>();
        data.put("valid", true);
        data.put("userId", tokenInfo.getUserId());
        data.put("expireTime", tokenInfo.getExpireTime());
        
        return ApiResult.success(data);
    }

    @DeleteMapping("/invalidate")
    public ApiResult<Void> invalidateToken(@RequestParam String token) {
        tokenService.invalidateToken(token);
        return ApiResult.success("Token 作废成功", null);
    }
}
