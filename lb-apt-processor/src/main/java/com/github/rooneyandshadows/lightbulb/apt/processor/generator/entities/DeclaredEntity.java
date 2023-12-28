package com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities;

import javax.lang.model.type.TypeMirror;

public abstract class DeclaredEntity {
    protected final String name;
    protected final TypeInformation typeInformation;

    public Variable(String name, TypeMirror typeMirror) {
        this.typeInformation = new TypeInformation(typeMirror);
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public final TypeInformation getTypeInformation() {
        return typeInformation;
    }
}
