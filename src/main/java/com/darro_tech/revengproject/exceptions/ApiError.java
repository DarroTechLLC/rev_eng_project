package com.darro_tech.revengproject.exceptions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard error response format for all API errors
 */
public class ApiError {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private boolean success;
    private List<String> details;
    
    public ApiError() {
        this.timestamp = LocalDateTime.now();
        this.details = new ArrayList<>();
        this.success = false;
    }
    
    public ApiError(int status, String error, String message) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }
    
    public void addDetail(String detail) {
        this.details.add(detail);
    }
} 