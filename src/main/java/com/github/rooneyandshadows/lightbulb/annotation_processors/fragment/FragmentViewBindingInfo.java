package com.github.rooneyandshadows.lightbulb.annotation_processors.fragment;

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.BindView;
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils;
import javax.lang.model.element.Element;

public class FragmentViewBindingInfo {
    private final String fieldName;
    private final String resourceName;
    private final String setterName;

    public FragmentViewBindingInfo(Element fieldElement, BindView annotation) {
        Element classElement = fieldElement.getEnclosingElement();
        this.fieldName = fieldElement.getSimpleName().toString();
        this.resourceName = annotation.name();
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
