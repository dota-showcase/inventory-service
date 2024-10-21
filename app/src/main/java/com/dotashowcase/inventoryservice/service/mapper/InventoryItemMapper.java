package com.dotashowcase.inventoryservice.service.mapper;

import com.dotashowcase.inventoryservice.model.InventoryItem;
import com.dotashowcase.inventoryservice.model.embedded.AccountInfo;
import com.dotashowcase.inventoryservice.model.embedded.ItemAttribute;
import com.dotashowcase.inventoryservice.model.embedded.ItemEquipment;
import com.dotashowcase.inventoryservice.steamclient.response.dto.AccountInfoDTO;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemAttributeDTO;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemDTO;
import com.dotashowcase.inventoryservice.steamclient.response.dto.ItemEquipDTO;

import java.util.ArrayList;
import java.util.List;

public class InventoryItemMapper {

    public List<InventoryItem> itemDtoToInventoryItem(List<ItemDTO> items) {
        List<InventoryItem> list = new ArrayList<>(items.size());
        for (ItemDTO item : items) {
            list.add(itemDtoToInventoryItem(item));
        }

        return list;
    }

    public InventoryItem itemDtoToInventoryItem(ItemDTO item) {
        InventoryItem inventoryItem = new InventoryItem();

        inventoryItem.setItemId(item.getId());
        inventoryItem.setOriginalId(item.getOriginal_id());
        inventoryItem.setDefIndex(item.getDefindex());
        inventoryItem.setLevel(item.getLevel());
        inventoryItem.setInventoryToken(item.getInventory());
        inventoryItem.setInventoryPosition(getInventoryPosition(item.getInventory()));
        inventoryItem.setQuantity(item.getQuantity());
        inventoryItem.setQuality(item.getQuality());
        inventoryItem.setIsTradable(item.getFlag_cannot_craft());
        inventoryItem.setIsCraftable(item.getFlag_cannot_craft());
        inventoryItem.setStyle(item.getStyle());
        inventoryItem.setCustomName(item.getCustom_name());
        inventoryItem.setCustomDesc(item.getCustom_desc());

        // Item Equipment
        List<ItemEquipDTO> equippedDTO = item.getEquipped();
        if (equippedDTO != null) {
            List<ItemEquipment> itemEquipmentList = new ArrayList<>(equippedDTO.size());

            for (ItemEquipDTO equippedItemDTO : equippedDTO) {
                itemEquipmentList.add(equippedDTOToItemEquipment(equippedItemDTO));
            }

            inventoryItem.setItemEquipment(itemEquipmentList);
        }

        // Item Attributes
        List<ItemAttributeDTO> attributesDTO = item.getAttributes();
        if (attributesDTO != null) {
            List<ItemAttribute> itemAttributesList = new ArrayList<>(attributesDTO.size());

            for (ItemAttributeDTO attributeDTO : attributesDTO) {
                itemAttributesList.add(itemAttributeDTOToItemAttribute(attributeDTO));
            }

            inventoryItem.setAttributes(itemAttributesList);
        }

        return inventoryItem;
    }

    public ItemEquipment equippedDTOToItemEquipment(ItemEquipDTO itemEquipDTO) {
        ItemEquipment itemEquipment = new ItemEquipment();
        itemEquipment.setEquipClass(itemEquipDTO.getEquip_class());
        itemEquipment.setSlot(itemEquipDTO.getSlot());

        return itemEquipment;
    }

    public ItemAttribute itemAttributeDTOToItemAttribute(ItemAttributeDTO attributeDTO) {
        ItemAttribute itemAttribute = new ItemAttribute();
        itemAttribute.setDefIndex(attributeDTO.getDefindex());
        itemAttribute.setFloatValue(attributeDTO.getFloat_value());
        itemAttribute.setValue(attributeDTO.getValue());

        // Account Info
        AccountInfoDTO accountInfoDTO = attributeDTO.getAccount_info();

        if (accountInfoDTO != null) {
            itemAttribute.setAccountInfo(accountInfoDTOToAccountInfo(accountInfoDTO));
        }

        return itemAttribute;
    }

    public AccountInfo accountInfoDTOToAccountInfo(AccountInfoDTO accountInfoDTO) {
        AccountInfo accountInfo = new AccountInfo();

        accountInfo.setSteamId(accountInfoDTO.getSteamid());
        accountInfo.setPersonalName(accountInfoDTO.getPersonaname());

        return accountInfo;
    }

    private int getInventoryPosition(Long inventoryToken) {
        if (inventoryToken == null) {
            return 0;
        }

        // lower two bytes
        return (int) (inventoryToken & 0xFFFF);
    }
}
