package com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.base;

import javax.lang.model.type.TypeMirror;

public abstract class DeclaredValueHolder {
    protected final String name;
    protected final TypeInformation typeInformation;

    public DeclaredValueHolder(String name, TypeMirror typeMirror) {
        this.name = name;
        this.typeInformation = new TypeInformation(typeMirror);
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
