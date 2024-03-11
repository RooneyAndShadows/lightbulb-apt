package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.base.BaseDefinition;

public abstract class BaseMetadata<T extends BaseDefinition<?>> {
    protected final T definition;

    public BaseMetadata(T definition) {
        this.definition = definition;
    }
}
