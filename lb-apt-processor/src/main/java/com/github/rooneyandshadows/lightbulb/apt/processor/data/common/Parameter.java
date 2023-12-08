package com.github.rooneyandshadows.lightbulb.apt.processor.data.common;

import javax.lang.model.element.Element;

@SuppressWarnings("DuplicatedCode")
public class Parameter extends Variable {
    private final boolean optional;

    public Parameter(Element element, boolean optional) {
        super(element);
        this.optional = optional;
    }

    public boolean isOptional() {
        return optional;
    }
}