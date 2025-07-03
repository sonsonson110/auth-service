package com.example.ecommerce.exception;

import java.util.List;
import java.util.Map;

public class ValidationException extends BusinessException{
    private final Map<String, List<String>> validationErrors;

    public ValidationException(String message) {
        super(message);
        this.validationErrors = null;
    }

    public ValidationException(Map<String, List<String>> validationErrors) {
        super("Validation error");
        this.validationErrors = validationErrors;
    }

    public Map<String, List<String>> getValidationErrors() {
        return validationErrors;
    }
}
