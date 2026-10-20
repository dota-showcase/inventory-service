package com.dotashowcase.inventoryservice.http.request;

import com.dotashowcase.inventoryservice.model.Operation;
import com.dotashowcase.inventoryservice.support.validator.TriggerConstraint;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateRequest {

    @TriggerConstraint
    @Schema(implementation = Operation.Trigger.class)
    private String trigger;

    public Operation.Trigger toTrigger() {
        return trigger != null ? Operation.Trigger.valueOf(trigger) : null;
    }
}
