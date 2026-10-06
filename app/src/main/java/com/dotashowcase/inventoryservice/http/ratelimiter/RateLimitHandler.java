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
        return consume(rateLimiter.resolveBucket(steamId), consumeCount, RateLimiter.LIMIT);
    }

    public HttpHeaders runUpdate(Long steamId) throws RateLimiterException {
        return consume(rateLimiter.resolveUpdateBucket(steamId), 1, RateLimiter.UPDATE_LIMIT);
    }

    private HttpHeaders consume(Bucket tokenBucket, int consumeCount, int limit) throws RateLimiterException {
        ConsumptionProbe probe = tokenBucket.tryConsumeAndReturnRemaining(consumeCount);

        HttpHeaders responseHeaders = new HttpHeaders();

        if (!probe.isConsumed()) {
            // round up - under 1s left must not become 0
            long waitForRefill = Math.ceilDiv(probe.getNanosToWaitForRefill(), 1_000_000_000L);

            throw new RateLimiterException("Allowed " + limit + " request(s) per minute", waitForRefill);
        }

        responseHeaders.set(HEADER_LIMIT_REMAINING, String.valueOf(probe.getRemainingTokens()));

        return responseHeaders;
    }
}
