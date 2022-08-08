package com.dotashowcase.inventoryservice.model;

import com.dotashowcase.inventoryservice.model.constant.HistoryActionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document("history_actions")
public class HistoryAction {

    @Id
    ObjectId id;

    @DocumentReference(lazy = true)
    Inventory inventory;

    @Field
    private HistoryActionType type = HistoryActionType.CREATE;

    @Field
    private Integer count = 0;          // stored item count

    @Field
    private Integer expectedCount = 0;       // expected item count / Steam API

    @Field
    private Integer numSlots = 0;       // num_backpack_slots / Steam API

    @Field
    private Date createdAt = new Date();
}
