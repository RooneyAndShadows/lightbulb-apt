package com.github.rooneyandshadows.lightbulb.apt.processor.definitions.base;

import javax.lang.model.AnnotatedConstruct;

public class BaseDefinition<T extends AnnotatedConstruct> {
    protected final T construct;

    public BaseDefinition(T construct) {
        this.construct = construct;
    }
}
