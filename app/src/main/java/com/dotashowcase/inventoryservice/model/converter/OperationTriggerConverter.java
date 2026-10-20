package com.dotashowcase.inventoryservice.model.converter;

import com.dotashowcase.inventoryservice.model.Operation;
import org.springframework.data.mongodb.core.convert.MongoConversionContext;
import org.springframework.data.mongodb.core.convert.MongoValueConverter;

// one letter code instead of the name - stored in every operation
public class OperationTriggerConverter implements MongoValueConverter<Operation.Trigger, String> {

    @Override
    public Operation.Trigger read(String value, MongoConversionContext context) {
        return Operation.Trigger.fromCode(value);
    }

    @Override
    public String write(Operation.Trigger value, MongoConversionContext context) {
        return value.getCode();
    }
}
