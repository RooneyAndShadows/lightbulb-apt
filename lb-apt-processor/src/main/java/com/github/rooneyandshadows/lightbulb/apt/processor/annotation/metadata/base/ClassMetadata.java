package com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base;

import javax.lang.model.element.TypeElement;

public abstract class ClassMetadata extends BaseMetadata<TypeElement> {
    public ClassMetadata(TypeElement element) {
        super(element);
    }

    public final String getClassSimpleName(){
        return element.getSimpleName().toString();
    }
}
