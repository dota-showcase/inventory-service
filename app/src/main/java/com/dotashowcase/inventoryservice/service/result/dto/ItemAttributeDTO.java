package com.dotashowcase.inventoryservice.service.result.dto;

import lombok.Data;

@Data
public class ItemAttributeDTO {

    private Integer defIndex;

    private String value;

    private Double floatValue;

    private AccountInfoDTO accountInfo;
}
