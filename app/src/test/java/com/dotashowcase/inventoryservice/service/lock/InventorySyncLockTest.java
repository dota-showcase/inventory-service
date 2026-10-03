package com.dotashowcase.inventoryservice.service.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class InventorySyncLockTest {

    private InventorySyncLock underTest;

    @BeforeEach
    void setUp() {
        underTest = new InventorySyncLock();
    }

    @Test
    void itShouldLockSteamId() {
        // given
        Long steamId = 76561198000000000L;

        // when
        boolean firstLock = underTest.tryLock(steamId);
        boolean secondLock = underTest.tryLock(steamId);

        // then
        assertThat(firstLock).isTrue();
        assertThat(secondLock).isFalse();
    }

    @Test
    void itShouldLockSteamIdsIndependently() {
        // given
        Long steamId1 = 76561198000000000L;
        Long steamId2 = 76561198000000001L;

        underTest.tryLock(steamId1);

        // when
        boolean otherLock = underTest.tryLock(steamId2);

        // then
        assertThat(otherLock).isTrue();
    }

    @Test
    void itShouldLockAgainAfterUnlock() {
        // given
        Long steamId = 76561198000000000L;

        underTest.tryLock(steamId);
        underTest.unlock(steamId);

        // when
        boolean lockAfterUnlock = underTest.tryLock(steamId);

        // then
        assertThat(lockAfterUnlock).isTrue();
    }

    @Test
    void itShouldAllowOnlyOneParallelLock() throws Exception {
        // given
        Long steamId = 76561198000000000L;
        int threadCount = 16;

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();

        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            for (int i = 0; i < threadCount; i++) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();

                    return underTest.tryLock(steamId);
                }));
            }

            // when
            // all threads try at once
            ready.await(5, TimeUnit.SECONDS);
            start.countDown();

            int lockCount = 0;
            for (Future<Boolean> result : results) {
                if (result.get(5, TimeUnit.SECONDS)) {
                    lockCount++;
                }
            }

            // then
            assertThat(lockCount).isEqualTo(1);
        }
    }
}
