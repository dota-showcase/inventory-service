package com.dotashowcase.inventoryservice.service.result.mapper;

import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.embedded.AccountInfo;
import com.dotashowcase.inventoryservice.model.embedded.ItemAttribute;
import com.dotashowcase.inventoryservice.model.embedded.ItemEquipment;
import com.dotashowcase.inventoryservice.service.result.dto.*;

import java.util.ArrayList;
import java.util.List;

public class InventoryItemServiceResultMapper {

    public InventoryItemDTO getInventoryItemDTO(InventoryItem inventoryItem) {
        InventoryItemDTO inventoryItemDTO = new InventoryItemDTO();

        inventoryItemDTO.setItemId(inventoryItem.getItemId());
        inventoryItemDTO.setOriginalId(inventoryItem.getOriginalId());
        inventoryItemDTO.setDefIndex(inventoryItem.getDefIndex());
        inventoryItemDTO.setLevel(inventoryItem.getLevel());
        inventoryItemDTO.setQuality(inventoryItem.getQuality());
        inventoryItemDTO.setInventoryToken(inventoryItem.getInventoryToken());
        inventoryItemDTO.setPosition(inventoryItem.getInventoryPosition());
        inventoryItemDTO.setUses(inventoryItem.getQuantity());
        inventoryItemDTO.setIsTradable(inventoryItem.getIsTradable());
        inventoryItemDTO.setIsCraftable(inventoryItem.getIsCraftable());
        inventoryItemDTO.setStyle(inventoryItem.getStyle());
        inventoryItemDTO.setCustomName(inventoryItem.getCustomName());
        inventoryItemDTO.setCustomDesc(inventoryItem.getCustomDesc());

        List<ItemEquipment> itemEquipments = inventoryItem.getItemEquipment();

        if (itemEquipments != null) {
            List<ItemEquipmentDTO> itemEquipmentDTOs = new ArrayList<>(itemEquipments.size());

            for (ItemEquipment itemEquipment : itemEquipments) {
                itemEquipmentDTOs.add(getItemEquipmentDTO(itemEquipment));
            }

            inventoryItemDTO.setEquipments(itemEquipmentDTOs);
        }

        List<ItemAttribute> itemAttributes = inventoryItem.getAttributes();

        if (itemAttributes != null) {
            List<ItemAttributeDTO> itemAttributeDTOs = new ArrayList<>(itemAttributes.size());

            for (ItemAttribute itemAttribute : itemAttributes) {
                itemAttributeDTOs.add(getItemAttributeDTO(itemAttribute));
            }

            inventoryItemDTO.setAttributes(itemAttributeDTOs);
        }

        return inventoryItemDTO;
    }

    private ItemEquipmentDTO getItemEquipmentDTO(ItemEquipment itemEquipment) {
        ItemEquipmentDTO itemEquipmentDTO = new ItemEquipmentDTO();
        itemEquipmentDTO.setSlot(itemEquipment.getSlot());
        itemEquipmentDTO.setEquipClass(itemEquipment.getEquipClass());

        return itemEquipmentDTO;
    }

    private ItemAttributeDTO getItemAttributeDTO(ItemAttribute itemAttribute) {
        ItemAttributeDTO itemAttributeDTO = new ItemAttributeDTO();
        itemAttributeDTO.setDefIndex(itemAttribute.getDefIndex());
        itemAttributeDTO.setValue(itemAttribute.getValue());
        itemAttributeDTO.setFloatValue(itemAttribute.getFloatValue());

        AccountInfo accountInfo = itemAttribute.getAccountInfo();

        if (accountInfo != null) {
            itemAttributeDTO.setAccountInfo(getAccountInfoDTO(accountInfo));
        }

        return itemAttributeDTO;
    }

    private AccountInfoDTO getAccountInfoDTO(AccountInfo accountInfo) {
        AccountInfoDTO accountInfoDTO = new AccountInfoDTO();
        accountInfoDTO.setSteamId(accountInfo.getSteamId());
        accountInfoDTO.setPersonalName(accountInfo.getPersonalName());

        return accountInfoDTO;
    }
}
