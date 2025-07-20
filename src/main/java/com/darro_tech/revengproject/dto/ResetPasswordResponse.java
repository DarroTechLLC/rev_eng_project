package com.darro_tech.revengproject.dto;

/**
 * DTO for reset password response that matches NextJS structure exactly. Maps
 * to: {success: boolean, error?: 'internal' | 'invalid-key' | 'expired-key' |
 * 'password-empty' | 'password-mismatch' | 'no-email' | 'send-email'}
 */
public class ResetPasswordResponse {

    private boolean success;
    private String error;

    public ResetPasswordResponse() {
    }

    public ResetPasswordResponse(boolean success) {
        this.success = success;
    }

    public ResetPasswordResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
