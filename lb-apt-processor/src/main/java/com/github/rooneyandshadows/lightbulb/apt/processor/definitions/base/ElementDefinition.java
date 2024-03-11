package com.github.rooneyandshadows.lightbulb.apt.processor.definitions.base;

import javax.lang.model.element.Element;

public abstract class ElementDefinition<T extends Element> extends BaseDefinition<T> {

    public ElementDefinition(T element) {
        super(element);
    }
}
