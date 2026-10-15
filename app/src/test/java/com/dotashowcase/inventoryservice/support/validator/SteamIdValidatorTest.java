package com.dotashowcase.inventoryservice.support.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SteamIdValidatorTest {

    private SteamIdValidator underTest;

    @BeforeEach
    void setUp() {
        underTest = new SteamIdValidator();
    }

    @Test
    void itShouldAcceptIndividualAccountIds() {
        // given
        // 76561197960265728 + 32-bit account id
        Long firstAccount = 76561197960265729L;
        Long newerAccount = 76561200000000000L; // past the old 7656119XXXXXXXXXX format
        Long lastAccount = 76561202255233023L;

        // when
        // then
        assertThat(underTest.isValid(firstAccount, null)).isTrue();
        assertThat(underTest.isValid(newerAccount, null)).isTrue();
        assertThat(underTest.isValid(lastAccount, null)).isTrue();
    }

    @Test
    void itShouldRejectIdsOutsideIndividualAccountRange() {
        // given
        Long accountIdZero = 76561197960265728L;
        Long accountIdOverflow = 76561202255233024L;
        Long notSteamId = 100000000000L;

        // when
        // then
        assertThat(underTest.isValid(null, null)).isFalse();
        assertThat(underTest.isValid(accountIdZero, null)).isFalse();
        assertThat(underTest.isValid(accountIdOverflow, null)).isFalse();
        assertThat(underTest.isValid(notSteamId, null)).isFalse();
    }
}
