package com.dotashowcase.inventoryservice.http.exception.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@AllArgsConstructor
@Data
public class ErrorResponse {

    private Instant timestamp;

    private int status;

    private String error;

    private String message;

    private String path;
}
