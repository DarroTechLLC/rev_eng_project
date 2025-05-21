package com.darro_tech.revengproject.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Generic API response object
 *
 * @param <T> Type of data being returned
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private T data;
    private String error;

    /**
     * Create a successful response with data
     *
     * @param data The data to return
     */
    public ApiResponse(T data) {
        this.data = data;
    }

    /**
     * Create an error response
     *
     * @param error The error message
     */
    public ApiResponse(String error) {
        this.error = error;
    }

    /**
     * Default constructor
     */
    public ApiResponse() {
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /**
     * Factory method to create a successful response
     *
     * @param data The data to return
     * @param <T> Type of data
     * @return An ApiResponse with the data
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }

    /**
     * Factory method to create an error response
     *
     * @param error The error message
     * @param <T> Type of data (unused in error case)
     * @return An ApiResponse with the error
     */
    public static <T> ApiResponse<T> error(String error) {
        return new ApiResponse<>(error);
    }
}
