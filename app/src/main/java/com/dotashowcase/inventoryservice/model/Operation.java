package com.dotashowcase.inventoryservice.model;

import com.dotashowcase.inventoryservice.model.converter.OperationTriggerConverter;
import com.dotashowcase.inventoryservice.model.embedded.OperationMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.convert.ValueConverter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
// unique - prevents duplicate versions on concurrent updates
@CompoundIndex(def = "{'steamId': 1, 'version': -1}", name = "operations__steam_id_version", unique = true)
@Document("operations")
public class Operation {

    @Id
    private ObjectId id;

    @Field
    private Long steamId;

    @Field
    private Integer version = 1;

    @Field
    private Type type = Type.C;

    // null - unknown: not passed or stored before triggers were tracked
    @Field("trg")
    @ValueConverter(OperationTriggerConverter.class)
    private Trigger trigger;

    // empty until sync stores counts - read mid-sync as an empty inventory
    @Field
    private OperationMeta meta = new OperationMeta();

    @Field
    private Date createdAt = new Date();

    public enum Type {
        C, U // Create, Update
    }

    // what started the sync; API value - name, stored value - one letter code
    public enum Trigger {
        auto("A"), manual("M"), resync("R");

        private final String code;

        Trigger(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static Trigger fromCode(String code) {
            for (Trigger trigger : values()) {
                if (trigger.code.equals(code)) {
                    return trigger;
                }
            }

            throw new IllegalArgumentException("Unknown Operation.Trigger code: " + code);
        }
    }
}
