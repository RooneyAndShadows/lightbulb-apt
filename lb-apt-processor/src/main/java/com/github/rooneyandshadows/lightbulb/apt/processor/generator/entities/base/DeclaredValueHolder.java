package com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.TypeDefinition;

public abstract class DeclaredValueHolder {
    protected final String name;
    protected final TypeDefinition typeInformation;

    public DeclaredValueHolder(String name, TypeDefinition typeInformation) {
        this.name = name;
        this.typeInformation = typeInformation;
    }

    public abstract String getValueAccessor();

    public abstract String getValueSetStatement(String setVarName);

    public final String getName() {
        return name;
    }

    public final TypeDefinition getTypeInformation() {
        return typeInformation;
    }
}
