package com.dotashowcase.inventoryservice.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class SortBuilderTest {

    private SortBuilder underTest;

    @BeforeEach
    void setUp() {
        underTest = new SortBuilder();
    }

    @Test
    void itShouldBuildFromRequest() {
        // given
        String paramName1 = null;
        String paramName2 = "a";
        String paramName3 = "ab";
        String paramName4 = "abc";
        String paramName5 = "-ab";
        String paramName6 = "-abc";

        // when
        Sort expected1 = underTest.fromRequestParam(paramName1);
        Sort expected2 = underTest.fromRequestParam(paramName2);
        Sort expected3 = underTest.fromRequestParam(paramName3);
        Sort expected4 = underTest.fromRequestParam(paramName4);
        Sort expected5 = underTest.fromRequestParam(paramName5);
        Sort expected6 = underTest.fromRequestParam(paramName6);

        // then
        assertThat(expected1).isNull();
        assertThat(expected2).isNull();

        assertThat(expected3.stream().toList().get(0).getProperty()).isEqualTo(paramName3);
        assertThat(expected3.stream().toList().get(0).getDirection()).isEqualTo(Sort.Direction.ASC);

        assertThat(expected4.stream().toList().get(0).getProperty()).isEqualTo(paramName4);
        assertThat(expected4.stream().toList().get(0).getDirection()).isEqualTo(Sort.Direction.ASC);

        assertThat(expected5.stream().toList().get(0).getProperty()).isEqualTo(paramName5.substring(1));
        assertThat(expected5.stream().toList().get(0).getDirection()).isEqualTo(Sort.Direction.DESC);

        assertThat(expected6.stream().toList().get(0).getProperty()).isEqualTo(paramName6.substring(1));
        assertThat(expected6.stream().toList().get(0).getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}