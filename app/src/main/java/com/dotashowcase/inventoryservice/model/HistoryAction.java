package com.dotashowcase.inventoryservice.model;

import com.dotashowcase.inventoryservice.model.embedded.HistoryActionMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document("history_actions")
public class HistoryAction {

    @Id
    private ObjectId id;

    @Field
    private Long steamId;

    @Field
    private Integer version = 1;

    @Field
    private Type type = Type.CREATE;

    @Field
    private HistoryActionMeta meta;

    @Field
    private Date createdAt = new Date();

    public enum Type {
        CREATE, UPDATE
    }
}
