package com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base;

import javax.lang.model.element.Element;

public class BaseMetadata<T extends Element> {
    protected T element;

    public BaseMetadata(T element) {
        this.element = element;
    }

    public T getElement() {
        return element;
    }
}
