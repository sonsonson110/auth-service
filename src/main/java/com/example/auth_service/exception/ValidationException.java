package com.example.auth_service.exception;

import java.util.List;
import java.util.Map;

public class ValidationException extends BusinessException{
    private final Map<String, List<String>> validationErrors;

    public ValidationException(String message) {
        super(message);
        this.validationErrors = null;
    }

    public ValidationException(String message, Map<String, List<String>> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public Map<String, List<String>> getValidationErrors() {
        return validationErrors;
    }
}
