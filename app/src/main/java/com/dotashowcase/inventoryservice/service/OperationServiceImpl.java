package com.dotashowcase.inventoryservice.service;

import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.model.Inventory;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import com.dotashowcase.inventoryservice.repository.OperationRepository;
import com.dotashowcase.inventoryservice.service.exception.OperationNotFoundException;
import com.dotashowcase.inventoryservice.service.result.dto.OperationCountDTO;
import com.dotashowcase.inventoryservice.service.result.dto.OperationDTO;
import com.dotashowcase.inventoryservice.service.result.dto.pagination.PageResult;
import com.dotashowcase.inventoryservice.service.result.mapper.OperationServiceResultMapper;
import com.dotashowcase.inventoryservice.service.result.mapper.PageMapper;
import com.dotashowcase.inventoryservice.support.SortBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OperationServiceImpl implements OperationService  {

    private final OperationRepository operationRepository;

    private final SortBuilder sortBuilder;

    private final PageMapper<Operation, OperationDTO> pageMapper;

    private final OperationServiceResultMapper operationServiceResultMapper;

    private static final Logger log = LoggerFactory.getLogger(OperationServiceImpl.class);

    @Autowired
    public OperationServiceImpl(
            OperationRepository operationRepository,
            SortBuilder sortBuilder,
            PageMapper<Operation, OperationDTO> pageMapper
    ) {
        Assert.notNull(operationRepository, "OperationRepository must not be null!");
        this.operationRepository = operationRepository;

        Assert.notNull(sortBuilder, "SortBuilder must not be null!");
        this.sortBuilder = sortBuilder;

        Assert.notNull(pageMapper, "PageMapper<Operation, OperationDTO> must not be null!");
        this.pageMapper = pageMapper;

        this.operationServiceResultMapper = new OperationServiceResultMapper();
    }

    @Override
    public Map<Long, Operation> getAllLatest(List<Long> inventorySteamIds) {
        List<Operation> operations = operationRepository.aggregateLatestByInventories(inventorySteamIds);

        Map<Long, Operation> result = new HashMap<>();

        for (Operation operation : operations) {
            Long steamId = operation.getSteamId();

            result.put(steamId, operation);
        }

        return result;
    }

    @Override
    public PageResult<OperationDTO> getPage(Inventory inventory, Pageable pageable, String sortBy) {
        Sort sort = sortBuilder.fromRequestParam(sortBy);

        Page<Operation> inventoryPage = operationRepository.findPage(inventory, pageable, sort);

        return pageMapper.getPageResult(inventoryPage, operationServiceResultMapper::getOperationDTO);
    }

    @Override
    public Operation getLatest(Inventory inventory) {
       return operationRepository.findLatest(inventory);
    }

    @Override
    public Operation getByVersion(Inventory inventory, Integer version) {
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
        Operation operation = new Operation();

        operation.setSteamId(inventory.getSteamId());

        if (type != null) {
            operation.setType(type);
        }

        if (prevOperation != null) {
            operation.setVersion(prevOperation.getVersion() + 1);
        }

        return operationRepository.insertOne(operation);
    }

    @Override
    public void createAndSaveMeta(Operation operation, OperationCountDTO operations, Integer count, Integer numSlots) {
        OperationMeta meta = new OperationMeta();

        meta.setItemCount(operations.getInventorySize());
        meta.setResponseCount(count);
        meta.setCreateOperationCount(operations.getCreate());
        meta.setUpdateOperationCount(operations.getUpdate());
        meta.setDeleteOperationCount(operations.getDelete());
        meta.setNumSlots(numSlots != null ? numSlots : -1);

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
