package com.dotashowcase.inventoryservice.support.exception;

import java.util.Set;
import java.util.TreeSet;

public class SortFieldNotAllowedException extends RuntimeException {

    public SortFieldNotAllowedException(String field, Set<String> allowedFields) {
        // sorted - Set.of order differs between runs
        super("Sorting by '" + field + "' is not allowed. Allowed: " + String.join(", ", new TreeSet<>(allowedFields)));
    }
}
