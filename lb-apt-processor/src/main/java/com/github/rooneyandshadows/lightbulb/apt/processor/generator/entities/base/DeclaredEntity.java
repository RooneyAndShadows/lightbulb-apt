package com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.BaseMetadata;

import javax.lang.model.type.TypeMirror;

public abstract class DeclaredEntity {
    protected final String name;
    protected final TypeInformation typeInformation;
    protected final BaseMetadata metadata;

    public DeclaredEntity(String name, TypeMirror typeMirror, BaseMetadata metadata) {
        this.name = name;
        this.typeInformation = new TypeInformation(typeMirror);
        this.metadata = metadata;
    }

    public DeclaredEntity(String name, TypeMirror typeMirror) {
        this.name = name;
        this.typeInformation = new TypeInformation(typeMirror);
        this.metadata = null;
    }


    public final String getName() {
        return name;
    }

    public final TypeInformation getTypeInformation() {
        return typeInformation;
    }

    public BaseMetadata getMetadata() {
        return metadata;
    }
}
