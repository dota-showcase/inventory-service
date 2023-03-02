package com.dotashowcase.inventoryservice.service.result.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(title="ItemAttribute")
@Data
public class ItemAttributeDTO {

    private Integer defIndex;

    private String value;

    private Double floatValue;

    private AccountInfoDTO accountInfo;
}
