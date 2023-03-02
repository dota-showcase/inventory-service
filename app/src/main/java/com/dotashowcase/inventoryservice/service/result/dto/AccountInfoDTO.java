package com.dotashowcase.inventoryservice.service.result.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(title="AccountInfo")
@Data
public class AccountInfoDTO {

    private Long steamId;

    private String personalName;
}
