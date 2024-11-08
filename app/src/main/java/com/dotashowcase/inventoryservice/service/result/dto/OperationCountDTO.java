package com.dotashowcase.inventoryservice.service.result.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OperationCountDTO {

    private int create = 0;

    private int update = 0;

    private int delete = 0;

    private int inventorySize = 0;

    public OperationCountDTO(int create, int update, int delete, int initInventorySize) {
        this.create = create;
        this.update = update;
        this.delete = delete;

        this.inventorySize = Math.max((initInventorySize + create - delete), 0);
    }
}
