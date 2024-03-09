package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base;

import javax.lang.model.element.Element;

public abstract class BaseMetadata<T extends Element> {
    protected final String name;
    protected final T element;

    public BaseMetadata(T element) {
        this.name = element.getSimpleName().toString();
        this.element = element;
    }

    public String getName() {
        return name;
    }

    public T getElement() {
        return element;
    }
}
