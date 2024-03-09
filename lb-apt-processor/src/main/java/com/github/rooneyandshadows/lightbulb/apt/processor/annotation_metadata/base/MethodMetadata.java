package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.MethodDefinition;

import javax.lang.model.element.ExecutableElement;

public abstract class MethodMetadata extends BaseMetadata<ExecutableElement> {
    protected final MethodDefinition definition;

    public MethodMetadata(ExecutableElement element) {
        super(element);
        this.definition = new MethodDefinition(element);
    }

    public MethodDefinition getDefinition() {
        return definition;
    }
}
