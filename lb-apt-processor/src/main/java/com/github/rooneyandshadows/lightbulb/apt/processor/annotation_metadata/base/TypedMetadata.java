package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.TypeDefinition;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public abstract class TypedMetadata extends BaseMetadata<TypeElement> {
    protected final TypeMirror typeMirror;
    protected final TypeDefinition typeDefinition;

    public TypedMetadata(TypeElement element) {
        super(element);
        this.typeMirror = element.asType();
        this.typeDefinition = new TypeDefinition(element.asType());
    }

    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    public TypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
