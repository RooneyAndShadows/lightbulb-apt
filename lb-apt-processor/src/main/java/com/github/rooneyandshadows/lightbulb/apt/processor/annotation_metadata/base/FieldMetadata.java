package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.FieldDefinition;

public abstract class FieldMetadata extends TypedMetadata {
    protected final FieldDefinition fieldDefinition;

    public FieldMetadata(FieldDefinition fieldDefinition) {
        super(fieldDefinition.getType());
        this.fieldDefinition = fieldDefinition;
    }
}
