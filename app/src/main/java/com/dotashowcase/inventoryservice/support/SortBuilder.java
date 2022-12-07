package com.dotashowcase.inventoryservice.support;

import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class SortBuilder {

    private static final Character DESC_SORT_PREFIX = '-';

    /**
     * @param paramName - name field to sort by, optionally prefixed with "-" to sort in DESC
     * @return Sort instance
     */
    public Sort fromRequestParam(String paramName) {
        if (paramName == null || paramName.length() < 2) {
            return null;
        }

        char prefix;
        try {
            prefix = paramName.charAt(0);
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            return null;
        }

        boolean isDesc = prefix == DESC_SORT_PREFIX;
        Sort.Direction direction = isDesc ? Sort.Direction.DESC : Sort.Direction.ASC;
        String name = isDesc ? paramName.substring(1) : paramName;

        return Sort.by(direction, name);
    }
}
