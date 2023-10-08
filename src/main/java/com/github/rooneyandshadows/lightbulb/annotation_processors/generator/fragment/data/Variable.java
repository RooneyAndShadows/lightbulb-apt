package com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data;

import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils;
import com.squareup.javapoet.TypeName;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;


@SuppressWarnings("DuplicatedCode")
public class Variable {
    protected final String name;
    protected final TypeName type;
    protected final String setterName;
    protected final String getterName;
    protected final Element element;
    protected final boolean isNullable;

    public Variable(Element fieldElement) {
        Element classElement = fieldElement.getEnclosingElement();
        this.element = fieldElement;
        this.name = ElementUtils.getSimpleName(fieldElement);
        this.type = ElementUtils.getTypeOfFieldElement(fieldElement);
        this.setterName = ElementUtils.scanForSetter(classElement, name);
        this.getterName = ElementUtils.scanForGetter(classElement, name);
        this.isNullable = fieldElement.getAnnotation(Nullable.class) != null;
    }

    public String getName() {
        return name;
    }

    public TypeName getType() {
        return type;
    }

    public String getSetterName() {
        return setterName;
    }

    public String getGetterName() {
        return getterName;
    }

    public boolean hasSetter() {
        return setterName != null;
    }

    public boolean hasGetter() {
        return getterName != null;
    }

    public boolean isNullable() {
        return isNullable;
    }
}