package com.example.ecommerce.dto;

import java.util.List;
import java.util.Map;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Map<String, List<String>> errors;

    private ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    private ApiResponse(boolean success, String message, Map<String, List<String>> errors) {
        this.success = success;
        this.message = message;
        this.errors = errors;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message);
    }

    public static ApiResponse<Void> error(String message, Map<String, List<String>> errors) {
        return new ApiResponse<>(false, message, errors);
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, List<String>> errors) {
        this.errors = errors;
    }
}
