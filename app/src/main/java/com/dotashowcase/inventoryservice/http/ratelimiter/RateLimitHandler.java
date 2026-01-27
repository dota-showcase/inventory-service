package com.dotashowcase.inventoryservice.http.ratelimiter;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class RateLimitHandler {

    public static final String HEADER_RETRY_AFTER = "X-Rate-Limit-Retry-After-Seconds";

    private static final String HEADER_LIMIT_REMAINING = "X-Rate-Limit-Remaining";

    private final RateLimiter rateLimiter;

    public RateLimitHandler(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public HttpHeaders run(Long steamId, int consumeCount) throws RateLimiterException {
        Bucket tokenBucket = rateLimiter.resolveBucket(steamId);
        ConsumptionProbe probe = tokenBucket.tryConsumeAndReturnRemaining(consumeCount);

        HttpHeaders responseHeaders = new HttpHeaders();

        if (!probe.isConsumed()) {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;

            throw new RateLimiterException("Allowed " + RateLimiter.LIMIT + " request(s) per minute", waitForRefill);
        }

        responseHeaders.set(HEADER_LIMIT_REMAINING, String.valueOf(probe.getRemainingTokens()));

        return responseHeaders;
    }
}
