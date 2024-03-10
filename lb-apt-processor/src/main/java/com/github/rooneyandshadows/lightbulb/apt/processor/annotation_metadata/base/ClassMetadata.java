package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.ClassDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.TypeDefinition;

public abstract class ClassMetadata extends TypedMetadata {
    protected final ClassDefinition classDefinition;

    public ClassMetadata(ClassDefinition classDefinition) {
        super(classDefinition.getType());
        this.classDefinition = classDefinition;
    }

    public final TypeDefinition getType() {
        return definition;
    }

    public final String getSimpleName() {
        return definition.getSimpleName();
    }

    public final String getQualifiedName() {
        return definition.getQualifiedName();
    }

    public final String getResolvedQualifiedName() {
        return definition.getQualifiedResolvedName();
    }

    public final String getResolvedSimpleName() {
        return definition.getSimpleResolvedName();
    }
}
