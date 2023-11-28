package com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.inner;

import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;
import com.squareup.javapoet.TypeName;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;


@SuppressWarnings("DuplicatedCode")
public class Variable {
    protected final String name;
    protected final TypeName type;
    protected final String setterName;
    protected final String getterName;
    protected final Element element;
    protected final Modifier accessModifier;
    protected final Modifier setterAccessModifier;
    protected final Modifier getterAccessModifier;
    protected final boolean isFinal;
    protected final boolean isNullable;

    public Variable(Element fieldElement) {
        Element classElement = fieldElement.getEnclosingElement();
        this.element = fieldElement;
        this.name = ElementUtils.getSimpleName(fieldElement);
        this.type = ElementUtils.getTypeOfFieldElement(fieldElement);
        this.setterName = ElementUtils.scanForSetter(classElement, name);
        this.getterName = ElementUtils.scanForGetter(classElement, name);
        this.accessModifier = ElementUtils.getAccessModifier(fieldElement);
        this.setterAccessModifier = ElementUtils.getMethodAccessModifier(classElement, setterName);
        this.getterAccessModifier = ElementUtils.getMethodAccessModifier(classElement, getterName);
        this.isFinal = ElementUtils.isFinal(fieldElement);
        this.isNullable = fieldElement.getAnnotation(Nullable.class) != null;
    }

    public boolean accessModifierAtLeast(Modifier target) {

        if (target == null || accessModifier == null) {
            return false;
        }

        return accessModifier.ordinal() <= target.ordinal();
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

    public boolean isFinal() {
        return isFinal;
    }

    public Modifier getAccessModifier() {
        return accessModifier;
    }

    public Modifier getSetterAccessModifier() {
        return setterAccessModifier;
    }

    public Modifier getGetterAccessModifier() {
        return getterAccessModifier;
    }
}