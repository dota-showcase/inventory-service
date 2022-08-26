package com.dotashowcase.inventoryservice.support;

import lombok.Data;

@Data
public final class HistoryRangeCriteria {

    private Integer prevVersion;

    private Integer version;

    public HistoryRangeCriteria(Integer version) {
        if (version != null) {
            this.prevVersion = version - 1;
            this.version = version;
        }
    }
}
