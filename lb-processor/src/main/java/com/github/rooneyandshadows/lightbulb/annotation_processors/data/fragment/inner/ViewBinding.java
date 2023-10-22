package com.github.rooneyandshadows.lightbulb.annotation_processors.data.fragment.inner;

import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils;

import javax.lang.model.element.Element;

public class ViewBinding {
    private final String fieldName;
    private final String resourceName;
    private final String setterName;

    public ViewBinding(Element fieldElement, String resourceName) {
        Element classElement = fieldElement.getEnclosingElement();
        this.fieldName = ElementUtils.getSimpleName(fieldElement);
        this.resourceName = resourceName;
        this.setterName = ElementUtils.scanForSetter(classElement, fieldName);
    }

    public String getSetterName() {
        return setterName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public boolean hasSetter() {
        return setterName != null;
    }
}
