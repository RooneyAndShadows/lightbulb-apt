package com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.TypeInformation;

public abstract class DeclaredValueHolder {
    protected final String name;
    protected final TypeInformation typeInformation;

    public DeclaredValueHolder(String name, TypeInformation typeInformation) {
        this.name = name;
        this.typeInformation = typeInformation;
    }

    public abstract String getValueAccessor();

    public abstract String getValueSetStatement(String setVarName);

    public final String getName() {
        return name;
    }

    public final TypeInformation getTypeInformation() {
        return typeInformation;
    }
}
