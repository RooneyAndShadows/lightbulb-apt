package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.TypeDefinition;

public abstract class TypedMetadata extends BaseMetadata<TypeDefinition> {

    public TypedMetadata(TypeDefinition definition) {
        super(definition);
    }
}
