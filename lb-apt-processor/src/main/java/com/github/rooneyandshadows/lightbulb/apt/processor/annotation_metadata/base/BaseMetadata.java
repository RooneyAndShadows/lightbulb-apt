package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.TypeInformation;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

public abstract class BaseMetadata<T extends Element> {
    protected final String name;
    protected final T element;
    protected final TypeMirror typeMirror;
    protected final TypeInformation typeInformation;

    public BaseMetadata(T element) {
        this.name = element.getSimpleName().toString();
        this.element = element;
        this.typeMirror = element.asType();
        this.typeInformation = new TypeInformation(element);
    }

    public String getName() {
        return name;
    }

    public T getElement() {
        return element;
    }

    public TypeMirror getTypeMirror(){
        return typeMirror;
    }

    public TypeInformation getTypeInformation() {
        return typeInformation;
    }
}
