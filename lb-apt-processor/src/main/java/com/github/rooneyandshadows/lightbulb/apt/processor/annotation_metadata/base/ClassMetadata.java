package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.ClassDefinition;

import javax.lang.model.element.TypeElement;

public abstract class ClassMetadata extends TypedMetadata {
    protected final ClassDefinition classDefinition;

    public ClassMetadata(TypeElement element) {
        super(element);
        this.classDefinition = new ClassDefinition(element);
    }

    public ClassMetadata(TypeElement element) {
        super(element);
    }

    public final String getSimpleName() {
        return name;
    }

    public final String getQualifiedName() {
        return typeDefinition.getQualifiedName();
    }

    public final String getResolvedQualifiedName() {
        return typeDefinition.getQualifiedResolvedName();
    }

    public final String getResolvedSimpleName() {
        return typeDefinition.getSimpleResolvedName();
    }
}
