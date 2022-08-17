package com.dotashowcase.inventoryservice.service.result.dto;

import com.dotashowcase.inventoryservice.model.HistoryAction;
import lombok.Data;

import java.util.Date;

@Data
public class HistoryActionDTO {

    private Integer version;

    private HistoryAction.Type type;

    private Integer operations;

    private Integer responseCount;

    private Integer numSlots;

    private Date createdAt;
}