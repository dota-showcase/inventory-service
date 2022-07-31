package com.dotashowcase.inventoryservice.steamclient.response.dto;

import lombok.Data;

@Data
public class ItemAttributeDTO {

    private int defindex;

    private String value;                    // mixed

    private double float_value;

    private AccountInfoDTO account_info;     // optional
}
