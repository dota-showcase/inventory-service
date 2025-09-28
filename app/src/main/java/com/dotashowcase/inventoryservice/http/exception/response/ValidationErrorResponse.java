package com.dotashowcase.inventoryservice.http.exception.response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ValidationErrorResponse extends ErrorResponse {

    private final List<String> validationErrors;

    public ValidationErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            List<String> errors
    ) {
        super(timestamp, status, error, message, path);
        this.validationErrors = errors;
    }

    public ValidationErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path
    ) {
        super(timestamp, status, error, message, path);
        this.validationErrors = new ArrayList<String>();
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
