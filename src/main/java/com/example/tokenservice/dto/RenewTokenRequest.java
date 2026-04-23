package com.example.tokenservice.dto;

import javax.validation.constraints.NotBlank;

public class RenewTokenRequest {

    @NotBlank(message = "Token cannot be empty")
    private String token;

    public RenewTokenRequest() {
    }

    public RenewTokenRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
