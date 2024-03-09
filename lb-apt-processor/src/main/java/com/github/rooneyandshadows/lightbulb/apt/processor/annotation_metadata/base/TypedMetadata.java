package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.TypeDefinition;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

public abstract class TypedMetadata<T extends Element> extends BaseMetadata<T> {
    protected final TypeMirror typeMirror;
    protected final TypeDefinition typeInformation;

    public TypedMetadata(T element) {
        super(element);
        this.typeMirror = element.asType();
        this.typeInformation = new TypeDefinition(element.asType());
    }

    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    public TypeDefinition getTypeInformation() {
        return typeInformation;
    }
}
