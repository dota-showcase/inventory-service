package com.dotashowcase.inventoryservice.http.ratelimiter;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS)
public class RateLimiterException extends RuntimeException {

    private final long waitForRefill;

    public RateLimiterException(String message, long waitForRefill) {
        super(message);
        this.waitForRefill = waitForRefill;
    }

    public long getWaitForRefill() {
        return waitForRefill;
    }
}
