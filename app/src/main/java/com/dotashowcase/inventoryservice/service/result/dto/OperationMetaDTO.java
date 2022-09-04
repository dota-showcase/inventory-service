package com.dotashowcase.inventoryservice.service.result.dto;

import lombok.Data;

@Data
public class OperationMetaDTO {

    private Integer itemCount;

    private Integer responseCount;

    private Integer created;

    private Integer updated;

    private Integer deleted;

    private Integer numSlots;
}
