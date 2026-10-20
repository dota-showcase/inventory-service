package com.dotashowcase.inventoryservice.support.validator;

import com.dotashowcase.inventoryservice.model.Operation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TriggerValidatorTest {

    private TriggerValidator underTest;

    @BeforeEach
    void setUp() {
        underTest = new TriggerValidator();
    }

    @Test
    void itShouldAcceptTriggerNames() {
        // given
        // when
        // then
        for (Operation.Trigger trigger : Operation.Trigger.values()) {
            assertThat(underTest.isValid(trigger.name(), null)).isTrue();
        }

        // not passed
        assertThat(underTest.isValid(null, null)).isTrue();
    }

    @Test
    void itShouldRejectUnknownTriggers() {
        // given
        String unknown = "scheduled";
        String storedCode = Operation.Trigger.auto.getCode();
        String upperCase = "AUTO";
        String ordinal = "0"; // json number, coerced to a string

        // when
        // then
        assertThat(underTest.isValid(unknown, null)).isFalse();
        assertThat(underTest.isValid(storedCode, null)).isFalse();
        assertThat(underTest.isValid(upperCase, null)).isFalse();
        assertThat(underTest.isValid(ordinal, null)).isFalse();
        assertThat(underTest.isValid("", null)).isFalse();
    }
}
