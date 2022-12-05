package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import com.dotashowcase.inventoryservice.repository.OperationRepository;
import com.dotashowcase.inventoryservice.service.exception.OperationNotFoundException;
import com.dotashowcase.inventoryservice.service.result.dto.OperationCountDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OperationServiceImpl implements OperationService  {

    private final OperationRepository operationRepository;

    private static final Logger log = LoggerFactory.getLogger(OperationServiceImpl.class);

    @Autowired
    public OperationServiceImpl(OperationRepository operationRepository) {
        Assert.notNull(operationRepository, "OperationRepository must not be null!");
        this.operationRepository = operationRepository;
    }

    @Override
    public Map<Long, List<Operation>> getAll(List<Long> inventoryIds) {
        List<Operation> Operations = operationRepository.findByInventories(inventoryIds);

        Map<Long, List<Operation>> result = new HashMap<>();

        for (Operation Operation : Operations) {
            Long steamId = Operation.getSteamId();

            if (result.containsKey(steamId)) {
                result.get(steamId).add(Operation);
            } else {
                result.put(steamId, new ArrayList<>(List.of(Operation)));
            }
        }

        return result;
    }

    @Override
    public List<Operation> getAll(Inventory inventory) {
        return operationRepository.findNLatest(inventory, -1);
    }

    @Override
    public Operation getLatest(Inventory inventory) {
       return operationRepository.findLatest(inventory);
    }

    @Override
    public Operation getByVersion(Inventory inventory, Integer version) {
        // by default try to find latest
        Operation operation = version == null
                ? operationRepository.findLatest(inventory)
                : operationRepository.findByVersion(inventory, version);

        if (operation == null) {
            if (version != null) {
                throw new OperationNotFoundException(inventory.getSteamId(), version);
            } else {
                throw new OperationNotFoundException(inventory.getSteamId());
            }
        }

        return operation;
    }

    @Override
    public Operation create(
            Inventory inventory,
            Operation.Type type,
            Operation prevOperation
    ) {
        Operation Operation = new Operation();

        Operation.setSteamId(inventory.getSteamId());

        if (type != null) {
            Operation.setType(type);
        }

        if (prevOperation != null) {
            Operation.setVersion(prevOperation.getVersion() + 1);
        }

        return operationRepository.insertOne(Operation);
    }

    @Override
    public void createAndSaveMeta(Operation operation, OperationCountDTO operations, Integer count, Integer numSlots) {
        OperationMeta meta = new OperationMeta();

        meta.setItemCount(operations.getInventorySize());
        meta.setResponseCount(count);
        meta.setCreateOperationCount(operations.getCreate());
        meta.setUpdateOperationCount(operations.getUpdate());
        meta.setDeleteOperationCount(operations.getDelete());
        meta.setNumSlots(numSlots);

        if (operationRepository.updateMeta(operation, meta) == 0) {
            log.warn("Failed to store OperationMeta {} for Operation _id - {}", meta, operation.getId());
        }

        operation.setMeta(meta);
    }

    @Override
    public long delete(Inventory inventory) {
        return operationRepository.removeAll(inventory);
    }
}
