package com.example.tokenservice.controller;

import com.example.tokenservice.model.TokenInfo;
import com.example.tokenservice.service.SecureTokenService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(TokenController.class)
public class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecureTokenService tokenService;

    private TokenInfo mockTokenInfo;

    @Before
    public void setUp() {
        mockTokenInfo = new TokenInfo();
        mockTokenInfo.setToken("test.access.token");
        mockTokenInfo.setRefreshToken("test.refresh.token");
        mockTokenInfo.setUserId("user123");
        mockTokenInfo.setExpireTime(System.currentTimeMillis() + 3600000);
    }

    @Test
    public void testGenerateToken_Success() throws Exception {
        when(tokenService.generateToken("user123")).thenReturn(mockTokenInfo);

        mockMvc.perform(post("/api/token/generate")
                .param("userId", "user123")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").value("test.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("test.refresh.token"))
                .andExpect(jsonPath("$.userId").value("user123"));
    }

    @Test
    public void testGenerateToken_EmptyUserId() throws Exception {
        mockMvc.perform(post("/api/token/generate")
                .param("userId", "")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("userId cannot be empty"));
    }

    @Test
    public void testRefreshToken_Success() throws Exception {
        when(tokenService.refreshToken("valid-refresh-token")).thenReturn(mockTokenInfo);

        mockMvc.perform(post("/api/token/refresh")
                .param("refreshToken", "valid-refresh-token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").value("test.access.token"));
    }

    @Test
    public void testRefreshToken_Invalid() throws Exception {
        when(tokenService.refreshToken(anyString())).thenReturn(null);

        mockMvc.perform(post("/api/token/refresh")
                .param("refreshToken", "invalid-token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("refresh token is invalid or expired"));
    }

    @Test
    public void testValidateToken_Valid() throws Exception {
        when(tokenService.validateToken("valid-token")).thenReturn(mockTokenInfo);

        mockMvc.perform(get("/api/token/validate")
                .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.userId").value("user123"));
    }

    @Test
    public void testValidateToken_Invalid() throws Exception {
        when(tokenService.validateToken(anyString())).thenReturn(null);

        mockMvc.perform(get("/api/token/validate")
                .param("token", "invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("token is invalid or expired"));
    }

    @Test
    public void testInvalidateToken_Success() throws Exception {
        when(tokenService.invalidateToken("valid-token")).thenReturn(true);

        mockMvc.perform(delete("/api/token/invalidate")
                .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("token invalidated successfully"));
    }

    @Test
    public void testInvalidateToken_NotFound() throws Exception {
        when(tokenService.invalidateToken(anyString())).thenReturn(false);

        mockMvc.perform(delete("/api/token/invalidate")
                .param("token", "non-existent-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("token not found or already invalidated"));
    }

    @Test
    public void testValidateToken_EmptyToken() throws Exception {
        mockMvc.perform(get("/api/token/validate")
                .param("token", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("token cannot be empty"));
    }
}
