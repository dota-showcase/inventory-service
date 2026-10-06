package com.dotashowcase.inventoryservice.http.ratelimiter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimitHandlerTest {

    private RateLimitHandler underTest;

    @BeforeEach
    void setUp() {
        underTest = new RateLimitHandler(new RateLimiter());
    }

    @Test
    void willThrowOnSecondUpdateWithinMinute() {
        // given
        Long steamId = 76561198000000000L;

        underTest.runUpdate(steamId);

        // when
        // then
        assertThatThrownBy(() -> underTest.runUpdate(steamId))
                .isInstanceOf(RateLimiterException.class)
                .hasMessage("Allowed 1 request(s) per minute");
    }

    @Test
    void itShouldRoundUpWaitForRefill() {
        // given
        Long steamId = 76561198000000000L;

        underTest.runUpdate(steamId);

        // when
        // then
        // just under 60s left
        assertThatThrownBy(() -> underTest.runUpdate(steamId))
                .isInstanceOfSatisfying(RateLimiterException.class,
                        exception -> assertThat(exception.getWaitForRefill()).isEqualTo(60));
    }

    @Test
    void itShouldAllowUpdateAfterCreate() {
        // given
        Long steamId = 76561198000000000L;

        underTest.run(steamId, 2);

        // when
        // then
        assertThatCode(() -> underTest.runUpdate(steamId)).doesNotThrowAnyException();
    }
}
