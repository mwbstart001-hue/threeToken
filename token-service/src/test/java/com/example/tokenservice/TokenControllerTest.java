package com.example.tokenservice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TokenControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private String validToken;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        
        String response = mockMvc.perform(post("/api/token/generate")
                .param("userId", "testUser"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        validToken = response.split("\"token\":\"")[1].split("\"")[0];
    }

    @Test
    public void generateToken_Success() throws Exception {
        mockMvc.perform(post("/api/token/generate")
                .param("userId", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.userId").value("user123"));
    }

    @Test
    public void generateToken_EmptyUserId_ShouldReturnError() throws Exception {
        mockMvc.perform(post("/api/token/generate")
                .param("userId", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1005))
                .andExpect(jsonPath("$.message").value("userId 不能为空"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    public void generateToken_MissingUserIdParam_ShouldReturnError() throws Exception {
        mockMvc.perform(post("/api/token/generate"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void validateToken_Success() throws Exception {
        mockMvc.perform(get("/api/token/validate")
                .param("token", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.userId").value("testUser"));
    }

    @Test
    public void validateToken_EmptyToken_ShouldReturnError() throws Exception {
        mockMvc.perform(get("/api/token/validate")
                .param("token", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.message").value("Token 不能为空"));
    }

    @Test
    public void validateToken_InvalidToken_ShouldReturnError() throws Exception {
        mockMvc.perform(get("/api/token/validate")
                .param("token", "invalidToken123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1002))
                .andExpect(jsonPath("$.message").value("Token 非法或不存在"));
    }

    @Test
    public void invalidateToken_Success() throws Exception {
        mockMvc.perform(delete("/api/token/invalidate")
                .param("token", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Token 作废成功"));
    }

    @Test
    public void invalidateToken_AlreadyInvalidated_ShouldReturnError() throws Exception {
        mockMvc.perform(delete("/api/token/invalidate")
                .param("token", validToken))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/token/validate")
                .param("token", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1002));
    }

    @Test
    public void invalidateToken_EmptyToken_ShouldReturnError() throws Exception {
        mockMvc.perform(delete("/api/token/invalidate")
                .param("token", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001))
                .andExpect(jsonPath("$.message").value("Token 不能为空"));
    }

    @Test
    public void test_UnifiedResponseFormat() throws Exception {
        mockMvc.perform(get("/api/token/validate")
                .param("token", "wrongToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists());
    }
}
