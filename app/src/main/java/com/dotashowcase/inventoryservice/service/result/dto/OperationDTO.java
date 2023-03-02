package com.dotashowcase.inventoryservice.service.result.dto;

import com.dotashowcase.inventoryservice.model.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Schema(title="Operation")
@Data
public class OperationDTO {

    private Integer version;

    private Operation.Type type;

    private Date createdAt;

    private OperationMetaDTO meta;
}
