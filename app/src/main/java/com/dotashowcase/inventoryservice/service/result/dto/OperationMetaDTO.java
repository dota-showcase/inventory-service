package com.dotashowcase.inventoryservice.service.result.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(title="OperationMeta")
@Data
public class OperationMetaDTO {

    private Integer itemCount;

    private Integer responseCount;

    private Integer created;

    private Integer updated;

    private Integer deleted;

    private Integer numSlots;
}
