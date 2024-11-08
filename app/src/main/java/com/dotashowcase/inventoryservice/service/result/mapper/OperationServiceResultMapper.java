package com.dotashowcase.inventoryservice.service.result.mapper;

import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import com.dotashowcase.inventoryservice.service.result.dto.*;

public class OperationServiceResultMapper {

    public OperationDTO getOperationDTO(Operation operation) {
        OperationDTO operationDTO = new OperationDTO();
        operationDTO.setVersion(operation.getVersion());
        operationDTO.setType(operation.getType());
        operationDTO.setCreatedAt(operation.getCreatedAt());

        OperationMeta meta = operation.getMeta();

        OperationMetaDTO operationMetaDTO = new OperationMetaDTO();

        operationMetaDTO.setItemCount(meta.getItemCount());
        operationMetaDTO.setResponseCount(meta.getResponseCount());
        operationMetaDTO.setCreated(meta.getCreateOperationCount());
        operationMetaDTO.setUpdated(meta.getUpdateOperationCount());
        operationMetaDTO.setDeleted(meta.getDeleteOperationCount());
        operationMetaDTO.setNumSlots(meta.getNumSlots());

        operationDTO.setMeta(operationMetaDTO);

        return operationDTO;
    }
}
