package com.dotashowcase.inventoryservice.support;

import com.dotashowcase.inventoryservice.support.exception.SortFieldNotAllowedException;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@NoArgsConstructor
public class SortBuilder {

    private static final char DESC_SORT_PREFIX = '-';

    /**
     * @param paramName - name field to sort by, optionally prefixed with "-" to sort in DESC
     * @param allowedFields - fields allowed to sort by
     * @return Sort instance, null on empty param
     * @throws SortFieldNotAllowedException on field not allowed
     */
    public Sort fromRequestParam(String paramName, Set<String> allowedFields) {
        if (paramName == null || paramName.isBlank()) {
            return null;
        }

        boolean isDesc = paramName.charAt(0) == DESC_SORT_PREFIX;
        Sort.Direction direction = isDesc ? Sort.Direction.DESC : Sort.Direction.ASC;
        String name = isDesc ? paramName.substring(1) : paramName;

        if (!allowedFields.contains(name)) {
            throw new SortFieldNotAllowedException(name, allowedFields);
        }

        return Sort.by(direction, name);
    }
}
