package com.darro_tech.revengproject.dto;

/**
 * DTO for forgot password request that matches NextJS structure exactly. Maps
 * to: {usernameOrEmail: string}
 */
public class ForgotPasswordRequest {

    private String usernameOrEmail;

    public ForgotPasswordRequest() {
    }

    public ForgotPasswordRequest(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }
}
