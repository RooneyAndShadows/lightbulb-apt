package com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common;

import javax.lang.model.element.Element;

@SuppressWarnings("DuplicatedCode")
public class Parameter extends Field {
    private final boolean optional;

    public Parameter(Element element, boolean optional) {
        super(element);
        this.optional = optional;
    }

    public boolean isOptional() {
        return optional;
    }
}