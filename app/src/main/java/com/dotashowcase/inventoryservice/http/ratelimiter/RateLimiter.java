package com.dotashowcase.inventoryservice.http.ratelimiter;

import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiter {

    public static final int LIMIT = 3;

    public static final int UPDATE_LIMIT = 1;

    private final Map<Long, Bucket> cache = new ConcurrentHashMap<>();

    private final Map<Long, Bucket> updateCache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(Long steamId) {
        return cache.computeIfAbsent(steamId, key -> newBucket(LIMIT));
    }

    public Bucket resolveUpdateBucket(Long steamId) {
        return updateCache.computeIfAbsent(steamId, key -> newBucket(UPDATE_LIMIT));
    }

    private Bucket newBucket(int capacity) {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(capacity).refillGreedy(1, Duration.ofMinutes(1)))
                .build();
    }
}
