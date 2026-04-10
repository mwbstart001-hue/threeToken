package com.example.tokenservice.controller;

import com.example.tokenservice.common.ErrorCode;
import com.example.tokenservice.model.TokenInfo;
import com.example.tokenservice.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGenerateToken_Success() throws Exception {
        TokenInfo mockToken = new TokenInfo("test-token-123", "user123", 
            System.currentTimeMillis(), System.currentTimeMillis() + 3600000);
        
        when(tokenService.generateToken("user123")).thenReturn(mockToken);
        
        mockMvc.perform(post("/api/token/generate")
                .param("userId", "user123")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.token").value("test-token-123"))
            .andExpect(jsonPath("$.data.userId").value("user123"));
    }

    @Test
    public void testGenerateToken_MissingParameter() throws Exception {
        mockMvc.perform(post("/api/token/generate")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.PARAM_ERROR.getCode()));
    }

    @Test
    public void testValidateToken_Success() throws Exception {
        TokenInfo mockToken = new TokenInfo("test-token-456", "user456", 
            System.currentTimeMillis(), System.currentTimeMillis() + 3600000);
        
        when(tokenService.validateToken("test-token-456")).thenReturn(mockToken);
        
        mockMvc.perform(get("/api/token/validate")
                .param("token", "test-token-456"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value("user456"));
    }

    @Test
    public void testValidateToken_MissingParameter() throws Exception {
        mockMvc.perform(get("/api/token/validate"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.PARAM_ERROR.getCode()));
    }

    @Test
    public void testInvalidateToken_Success() throws Exception {
        mockMvc.perform(delete("/api/token/invalidate")
                .param("token", "test-token-789"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testInvalidateToken_MissingParameter() throws Exception {
        mockMvc.perform(delete("/api/token/invalidate"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.PARAM_ERROR.getCode()));
    }
}
