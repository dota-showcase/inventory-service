package com.dotashowcase.inventoryservice.service.result.dto;

import com.dotashowcase.inventoryservice.model.Operation;
import lombok.Data;

import java.util.Date;

@Data
public class OperationDTO {

    private Integer version;

    private Operation.Type type;

    private Integer operations;

    private Integer responseCount;

    private Integer numSlots;

    private Date createdAt;
}