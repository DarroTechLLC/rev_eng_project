package com.darro_tech.revengproject.dto;

/**
 * DTO for forgot password response that matches NextJS structure exactly. Maps
 * to: {success: boolean, error?: 'internal' | 'no-email' | 'send-email'}
 */
public class ForgotPasswordResponse {

    private boolean success;
    private String error;

    public ForgotPasswordResponse() {
    }

    public ForgotPasswordResponse(boolean success) {
        this.success = success;
    }

    public ForgotPasswordResponse(boolean success, String error) {
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
