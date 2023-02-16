package com.dotashowcase.inventoryservice.steamclient.response;

import com.dotashowcase.inventoryservice.steamclient.response.dto.UserInventoryResponseDTO;
import com.dotashowcase.inventoryservice.steamclient.response.exception.BadResponseBodyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserInventoryResponseParserTest {

    private UserInventoryResponseParser underTest;

    @BeforeEach
    void setUp() {
        underTest = new UserInventoryResponseParser();
    }

    @Test
    void canParseNormalResponse() {
        // given
        String responseBody = """
                {
                    "result": {
                        "status": 1,
                        "num_backpack_slots": 10120,
                        "items": [
                            {
                                "id": 3120140571,
                                "original_id": 3053978672,
                                "defindex": 6671,
                                "level": 1,
                                "quality": 9,
                                "inventory": 1133,
                                "quantity": 1,
                                "equipped": [
                                    {
                                        "class": 13,
                                        "slot": 1
                                    },
                                    {
                                        "class": 13,
                                        "slot": 5
                                    }
                                ],
                                "attributes": [
                                    {
                                        "defindex": 321,
                                        "value": 1157349376,
                                        "float_value": 2014
                                    },
                                    {
                                        "defindex": 2010,
                                        "value": 1,
                                        "float_value": 1.401298464324817e-45
                                    }
                                ]
                            },
                            {
                                "id": 8178590185,
                                "original_id": 2764010163,
                                "defindex": 6450,
                                "level": 1,
                                "quality": 4,
                                "inventory": 1105,
                                "quantity": 1,
                                "custom_name": "Evil Magma",
                                "attributes": [
                                    {
                                        "defindex": 402,
                                        "value": "Evil Magma"
                                    }
                                ]
                            },
                            {
                                "id": 8348787622,
                                "original_id": 7697029422,
                                "defindex": 7559,
                                "level": 1,
                                "quality": 4,
                                "inventory": 9675,
                                "quantity": 1,
                                "flag_cannot_trade": true,
                                "flag_cannot_craft": true,
                                "custom_desc": "Test 123",
                                "equipped": [
                                    {
                                        "class": 10,
                                        "slot": 2
                                    }
                                ],
                                "attributes": [
                                    {
                                        "defindex": 186,
                                        "value": 174287302,
                                        "float_value": 1.0949546206917292e-32,
                                        "account_info": {
                                            "steamid": 76561198134553030,
                                            "personaname": "Joe"
                                        }
                                    }
                                ]
                            }
                        ]
                    }
                }""";

        // when
        UserInventoryResponseDTO expected = underTest.run(responseBody);

        // then
        assertThat(expected.hasItems()).isEqualTo(true);
        assertThat(expected.getStatus()).isEqualTo(1);
        assertThat(expected.getNumberBackpackSlots()).isEqualTo(10120);

        assertThat(expected.getItems().size()).isEqualTo(3);
        assertThat(expected.getItems().get(0).getId()).isEqualTo(3120140571L);
        assertThat(expected.getItems().get(0).getOriginal_id()).isEqualTo(3053978672L);
        assertThat(expected.getItems().get(0).getDefindex()).isEqualTo(6671);
        assertThat(expected.getItems().get(0).getLevel()).isEqualTo((byte) 1);
        assertThat(expected.getItems().get(0).getQuality()).isEqualTo((byte) 9);
        assertThat(expected.getItems().get(0).getInventory()).isEqualTo(1133);
        assertThat(expected.getItems().get(0).getQuantity()).isEqualTo(1);

        assertThat(expected.getItems().get(0).getEquipped().size()).isEqualTo(2);
        assertThat(expected.getItems().get(0).getEquipped().get(0).getEquip_class()).isEqualTo(13);
        assertThat(expected.getItems().get(0).getEquipped().get(0).getSlot()).isEqualTo(1);
        assertThat(expected.getItems().get(0).getEquipped().get(1).getEquip_class()).isEqualTo(13);
        assertThat(expected.getItems().get(0).getEquipped().get(1).getSlot()).isEqualTo(5);

        assertThat(expected.getItems().get(0).getAttributes().size()).isEqualTo(2);
        assertThat(expected.getItems().get(0).getAttributes().get(0).getDefindex()).isEqualTo(321);
        assertThat(expected.getItems().get(0).getAttributes().get(0).getValue()).isEqualTo("1157349376");
        assertThat(expected.getItems().get(0).getAttributes().get(0).getFloat_value()).isEqualTo(2014.0);
        assertThat(expected.getItems().get(0).getAttributes().get(1).getDefindex()).isEqualTo(2010);
        assertThat(expected.getItems().get(0).getAttributes().get(1).getValue()).isEqualTo("1");
        assertThat(expected.getItems().get(0).getAttributes().get(1).getFloat_value()).isEqualTo(1.401298464324817e-45);

        assertThat(expected.getItems().get(1).getId()).isEqualTo(8178590185L);
        assertThat(expected.getItems().get(1).getOriginal_id()).isEqualTo(2764010163L);
        assertThat(expected.getItems().get(1).getDefindex()).isEqualTo(6450);
        assertThat(expected.getItems().get(1).getLevel()).isEqualTo((byte) 1);
        assertThat(expected.getItems().get(1).getQuality()).isEqualTo((byte) 4);
        assertThat(expected.getItems().get(1).getInventory()).isEqualTo(1105);
        assertThat(expected.getItems().get(1).getQuantity()).isEqualTo(1);
        assertThat(expected.getItems().get(1).getCustom_name()).isEqualTo("Evil Magma");

        assertThat(expected.getItems().get(1).getEquipped()).isNull();

        assertThat(expected.getItems().get(1).getAttributes().size()).isEqualTo(1);
        assertThat(expected.getItems().get(1).getAttributes().get(0).getDefindex()).isEqualTo(402);
        assertThat(expected.getItems().get(1).getAttributes().get(0).getValue()).isEqualTo("Evil Magma");
        assertThat(expected.getItems().get(1).getAttributes().get(0).getFloat_value()).isNull();

        assertThat(expected.getItems().get(2).getId()).isEqualTo(8348787622L);
        assertThat(expected.getItems().get(2).getOriginal_id()).isEqualTo(7697029422L);
        assertThat(expected.getItems().get(2).getDefindex()).isEqualTo(7559);
        assertThat(expected.getItems().get(2).getLevel()).isEqualTo((byte) 1);
        assertThat(expected.getItems().get(2).getQuality()).isEqualTo((byte) 4);
        assertThat(expected.getItems().get(2).getInventory()).isEqualTo(9675);
        assertThat(expected.getItems().get(2).getQuantity()).isEqualTo(1);
        assertThat(expected.getItems().get(2).getFlag_cannot_trade()).isEqualTo(true);
        assertThat(expected.getItems().get(2).getFlag_cannot_craft()).isEqualTo(true);
        assertThat(expected.getItems().get(2).getCustom_desc()).isEqualTo("Test 123");

        assertThat(expected.getItems().get(2).getEquipped().size()).isEqualTo(1);
        assertThat(expected.getItems().get(2).getEquipped().get(0).getEquip_class()).isEqualTo(10);
        assertThat(expected.getItems().get(2).getEquipped().get(0).getSlot()).isEqualTo(2);

        assertThat(expected.getItems().get(2).getAttributes().size()).isEqualTo(1);
        assertThat(expected.getItems().get(2).getAttributes().get(0).getDefindex()).isEqualTo(186);
        assertThat(expected.getItems().get(2).getAttributes().get(0).getValue()).isEqualTo("174287302");
        assertThat(expected.getItems().get(2).getAttributes().get(0).getFloat_value())
                .isEqualTo(1.0949546206917292e-32);
        assertThat(expected.getItems().get(2).getAttributes().get(0).getAccount_info().getSteamid())
                .isEqualTo(76561198134553030L);
        assertThat(expected.getItems().get(2).getAttributes().get(0).getAccount_info().getPersonaname())
                .isEqualTo("Joe");
    }

    @Test
    void willThrowWhenJsonInvalid() {
        // given
        String responseBody = """
                {
                   "result": {
                       "status": 1,""";


        // when
        // then
        assertThatThrownBy(() -> underTest.run(responseBody))
                .isInstanceOf(BadResponseBodyException.class);
    }

    @Test
    void willThrowWhenRootNodeIsMissing() {
        // given
        String responseBody = """
                {
                    "status": 1,
                    "num_backpack_slots": 10120,
                    "items": []
                }""";


        // when
        // then
        assertThatThrownBy(() -> underTest.run(responseBody))
                .isInstanceOf(BadResponseBodyException.class)
                .hasMessageContaining(String.format("Body is mission required node - '%s'", "result"));
    }

    @Test
    void canParseWhenNoStatus() {
        // given
        String responseBody = """
                {
                    "result": {
                        "num_backpack_slots": 10120,
                        "items": []
                    }
                }""";

        // when
        UserInventoryResponseDTO expected = underTest.run(responseBody);

        // then
        assertThat(expected.hasItems()).isEqualTo(false);
        assertThat(expected.getStatus()).isNull();
        assertThat(expected.getNumberBackpackSlots()).isEqualTo(10120);

        assertThat(expected.getItems().size()).isEqualTo(0);
    }

    @Test
    void canParseWhenNoSlots() {
        // given
        String responseBody = """
                {
                    "result": {
                        "status": 1,
                        "items": []
                    }
                }""";

        // when
        UserInventoryResponseDTO expected = underTest.run(responseBody);

        // then
        assertThat(expected.hasItems()).isEqualTo(false);
        assertThat(expected.getStatus()).isEqualTo(1);
        assertThat(expected.getNumberBackpackSlots()).isNull();

        assertThat(expected.getItems().size()).isEqualTo(0);
    }

    @Test
    void canParseWhenNoItems() {
        // given
        String responseBody = """
                {
                    "result": {
                        "num_backpack_slots": 10120,
                        "status": 1
                    }
                }""";

        // when
        UserInventoryResponseDTO expected = underTest.run(responseBody);

        // then
        assertThat(expected.hasItems()).isEqualTo(false);
        assertThat(expected.getStatus()).isEqualTo(1);
        assertThat(expected.getNumberBackpackSlots()).isEqualTo(10120);

        assertThat(expected.getItems()).isNull();
    }

}