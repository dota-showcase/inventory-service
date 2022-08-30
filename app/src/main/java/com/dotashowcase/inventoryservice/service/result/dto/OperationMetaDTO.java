package com.dotashowcase.inventoryservice.service.result.dto;

import lombok.Data;

@Data
public class OperationMetaDTO {

    private Integer createCount;

    private Integer updateCount;

    private Integer deleteCount;

    private Integer responseCount;

    private Integer numSlots;
}
