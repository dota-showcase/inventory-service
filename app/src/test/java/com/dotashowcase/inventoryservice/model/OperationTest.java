package com.dotashowcase.inventoryservice.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperationTest {

    @Test
    void itShouldKeepStoredTriggerCodes() {
        // stored in existing operations - a changed code can not be read back
        assertThat(Operation.Trigger.auto.getCode()).isEqualTo("A");
        assertThat(Operation.Trigger.manual.getCode()).isEqualTo("M");
        assertThat(Operation.Trigger.resync.getCode()).isEqualTo("R");
    }

    @Test
    void itShouldGetTriggerByCode() {
        // also fails on a duplicate code - read back as another trigger
        for (Operation.Trigger trigger : Operation.Trigger.values()) {
            assertThat(Operation.Trigger.fromCode(trigger.getCode())).isSameAs(trigger);
        }
    }

    @Test
    void willThrowWhenTriggerCodeUnknown() {
        assertThatThrownBy(() -> Operation.Trigger.fromCode("X"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("X");
    }
}
