package com.darro_tech.revengproject.dto;

/**
 * DTO for reset password request that matches NextJS structure exactly. Maps
 * to: {username: string, resetKey: string, newPassword: string,
 * confirmPassword: string}
 */
public class ResetPasswordRequest {

    private String username;
    private String resetKey;
    private String newPassword;
    private String confirmPassword;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String username, String resetKey, String newPassword, String confirmPassword) {
        this.username = username;
        this.resetKey = resetKey;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getResetKey() {
        return resetKey;
    }

    public void setResetKey(String resetKey) {
        this.resetKey = resetKey;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
