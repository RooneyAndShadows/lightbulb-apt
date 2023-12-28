package com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities;

import javax.lang.model.element.Element;

@SuppressWarnings("DuplicatedCode")
public class FieldScreenParameter extends Field {
    private final boolean optional;

    public FieldScreenParameter(Element element, boolean optional) {
        super(element);
        this.optional = optional;
    }

    public boolean isOptional() {
        return optional;
    }
}