package com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.BaseMetadata;

import javax.lang.model.type.TypeMirror;

public abstract class DeclaredEntity {
    protected final String name;
    protected final TypeInformation typeInformation;

    public DeclaredEntity(String name, TypeMirror typeMirror) {
        this.name = name;
        this.typeInformation = new TypeInformation(typeMirror);
    }

    public final String getName() {
        return name;
    }

    public final TypeInformation getTypeInformation() {
        return typeInformation;
    }
}
