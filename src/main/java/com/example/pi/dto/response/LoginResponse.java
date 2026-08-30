package com.example.pi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private String token;
    private String refreshToken;
    private String userName;
    private String roles;
    private Integer customerId;
    private String message;

    public LoginResponse() {
    }

    public LoginResponse(String token, String userName, String roles, Integer customerId) {
        this.token = token;
        this.userName = userName;
        this.roles = roles;
        this.customerId = customerId;
    }

    public LoginResponse(String token, String refreshToken, String userName, String roles, Integer customerId) {
        this.token        = token;
        this.refreshToken = refreshToken;
        this.userName     = userName;
        this.roles        = roles;
        this.customerId   = customerId;
    }

    public LoginResponse(String message) {
        this.message = message;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
