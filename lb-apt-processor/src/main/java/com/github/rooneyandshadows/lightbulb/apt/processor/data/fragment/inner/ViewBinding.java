package com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.inner;

import javax.lang.model.element.Element;

public class ViewBinding extends ClassField {
    private final String resourceName;

    public ViewBinding(Element fieldElement, String resourceName) {
        super(fieldElement);
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }
}
