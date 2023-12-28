package com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities;

import javax.lang.model.element.Element;

public class FieldViewBinding extends Field {
    private final String resourceName;

    public FieldViewBinding(Element fieldElement, String resourceName) {
        super(fieldElement);
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }
}
