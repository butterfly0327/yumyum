package com.yumyumcoach.model.dto;

public class AuthStatus {
    private boolean authenticated;
    private String username;

    public AuthStatus() {
    }

    public AuthStatus(boolean authenticated, String username) {
        this.authenticated = authenticated;
        this.username = username;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
