package com.dotashowcase.inventoryservice.steamclient.response.dto;

import lombok.Data;

@Data
public class ItemAttributeDTO {

    private Integer defindex;

    private String value;                    // mixed

    private Double float_value;

    private AccountInfoDTO account_info;     // optional
}
