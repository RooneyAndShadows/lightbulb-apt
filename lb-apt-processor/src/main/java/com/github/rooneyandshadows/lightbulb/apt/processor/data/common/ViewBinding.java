package com.github.rooneyandshadows.lightbulb.apt.processor.data.common;

import javax.lang.model.element.Element;

public class ViewBinding extends Field {
    private final String resourceName;

    public ViewBinding(Element fieldElement, String resourceName) {
        super(fieldElement);
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }
}
