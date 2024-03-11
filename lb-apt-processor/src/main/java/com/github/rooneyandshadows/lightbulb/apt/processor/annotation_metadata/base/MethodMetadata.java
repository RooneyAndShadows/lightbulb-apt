package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.MethodDefinition;

import javax.lang.model.element.Modifier;


public abstract class MethodMetadata extends BaseMetadata<MethodDefinition> {
    protected final MethodDefinition methodDefinition;

    public MethodMetadata(MethodDefinition methodDefinition) {
        super(methodDefinition);
        this.methodDefinition = methodDefinition;
    }

    public MethodDefinition getMethod() {
        return methodDefinition;
    }

    public Modifier getAccessModifier(){
        return methodDefinition.getAccessModifier();
    }
}
