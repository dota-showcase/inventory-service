package com.dotashowcase.inventoryservice.support;

import com.dotashowcase.inventoryservice.support.exception.SortFieldNotAllowedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SortBuilderTest {

    private static final Set<String> ALLOWED_FIELDS = Set.of("ab", "abc");

    private SortBuilder underTest;

    @BeforeEach
    void setUp() {
        underTest = new SortBuilder();
    }

    @Test
    void itShouldBuildFromRequest() {
        // given
        String paramName1 = null;
        String paramName2 = "";
        String paramName3 = "ab";
        String paramName4 = "abc";
        String paramName5 = "-ab";
        String paramName6 = "-abc";

        // when
        Sort expected1 = underTest.fromRequestParam(paramName1, ALLOWED_FIELDS);
        Sort expected2 = underTest.fromRequestParam(paramName2, ALLOWED_FIELDS);
        Sort expected3 = underTest.fromRequestParam(paramName3, ALLOWED_FIELDS);
        Sort expected4 = underTest.fromRequestParam(paramName4, ALLOWED_FIELDS);
        Sort expected5 = underTest.fromRequestParam(paramName5, ALLOWED_FIELDS);
        Sort expected6 = underTest.fromRequestParam(paramName6, ALLOWED_FIELDS);

        // then
        assertThat(expected1).isNull();
        assertThat(expected2).isNull();

        assertThat(expected3.stream().toList().getFirst().getProperty()).isEqualTo(paramName3);
        assertThat(expected3.stream().toList().getFirst().getDirection()).isEqualTo(Sort.Direction.ASC);

        assertThat(expected4.stream().toList().getFirst().getProperty()).isEqualTo(paramName4);
        assertThat(expected4.stream().toList().getFirst().getDirection()).isEqualTo(Sort.Direction.ASC);

        assertThat(expected5.stream().toList().getFirst().getProperty()).isEqualTo(paramName5.substring(1));
        assertThat(expected5.stream().toList().getFirst().getDirection()).isEqualTo(Sort.Direction.DESC);

        assertThat(expected6.stream().toList().getFirst().getProperty()).isEqualTo(paramName6.substring(1));
        assertThat(expected6.stream().toList().getFirst().getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void willThrowWhenFieldNotAllowed() {
        // given
        String paramName = "-name";

        // when
        // then
        assertThatThrownBy(() -> underTest.fromRequestParam(paramName, ALLOWED_FIELDS))
                .isInstanceOf(SortFieldNotAllowedException.class)
                .hasMessage("Sorting by 'name' is not allowed. Allowed: ab, abc");
    }
}