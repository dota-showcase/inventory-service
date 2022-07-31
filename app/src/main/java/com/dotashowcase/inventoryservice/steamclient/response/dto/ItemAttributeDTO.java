package com.dotashowcase.inventoryservice.steamclient.response.dto;

import lombok.Data;

@Data
public class ItemAttributeDTO {

    public int defindex;

    public String value;                    // mixed

    public double float_value;

    public AccountInfoDTO account_info;     // optional
}
