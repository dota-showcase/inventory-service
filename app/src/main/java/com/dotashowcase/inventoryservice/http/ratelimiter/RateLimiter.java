package com.dotashowcase.inventoryservice.http.ratelimiter;

import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiter {

    public static final int LIMIT = 3;

    private final Map<Long, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(Long steamId) {
        return cache.computeIfAbsent(steamId, this::newBucket);
    }

    private Bucket newBucket(Long steamId) {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(LIMIT).refillGreedy(1, Duration.ofMinutes(1)))
                .build();
    }
}
