package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base;

import javax.lang.model.element.TypeElement;

public abstract class ClassMetadata extends BaseMetadata<TypeElement> {
    public ClassMetadata(TypeElement element) {
        super(element);
    }

    public final String getSimpleName() {
        return typeInformation.getSimpleName();
    }

    public final String getQualifiedName() {
        return typeInformation.getQualifiedName();
    }

    public final String getResolvedQualifiedName() {
        return typeInformation.getQualifiedResolvedName();
    }

    public final String getResolvedSimpleName() {
        return typeInformation.getSimpleResolvedName();
    }
}
