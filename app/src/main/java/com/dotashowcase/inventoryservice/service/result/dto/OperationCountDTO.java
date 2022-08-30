package com.dotashowcase.inventoryservice.service.result.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationCountDTO {

    private int create = 0;

    private int update = 0;

    private int delete = 0;
}
