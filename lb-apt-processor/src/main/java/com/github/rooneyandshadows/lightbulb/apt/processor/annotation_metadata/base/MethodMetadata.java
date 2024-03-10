package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.MethodDefinition;

public abstract class MethodMetadata extends BaseMetadata<MethodDefinition> {
    protected final MethodDefinition methodDefinition;

    public MethodMetadata(MethodDefinition methodDefinition) {
        super(methodDefinition);
        this.methodDefinition = methodDefinition;
    }

    public MethodDefinition getDefinition() {
        return methodDefinition;
    }
}
