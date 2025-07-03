package com.example.ecommerce.exception;

public class DuplicateResourceException extends BusinessException {
    private final String resourceType;
    private final String field;
    private final String value;

    public DuplicateResourceException(String resourceType, String field, String value) {
        super(String.format("%s with %s '%s' already exists", resourceType, field, value));
        this.resourceType = resourceType;
        this.field = field;
        this.value = value;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}
