package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public abstract class FieldMetadata extends TypedMetadata<VariableElement> {
    private final String setterName;
    private final String getterName;
    private final Modifier accessModifier;
    private final Modifier setterAccessModifier;
    private final Modifier getterAccessModifier;
    private final boolean isFinal;
    private final boolean isNullable;

    public FieldMetadata(VariableElement element) {
        super(element);
        TypeElement enclosingClassElement = (TypeElement) element.getEnclosingElement();
        this.setterName = ElementUtils.findFieldSetterName(enclosingClassElement,name);
        this.getterName = ElementUtils.findFieldGetterName(enclosingClassElement,name);
        this.accessModifier = ElementUtils.getAccessModifier(element);
        this.setterAccessModifier = ElementUtils.getMethodAccessModifier(enclosingClassElement, setterName);
        this.getterAccessModifier = ElementUtils.getMethodAccessModifier(enclosingClassElement, getterName);
        this.isFinal = ElementUtils.isFinal(element);
        this.isNullable = element.getAnnotation(Nullable.class) != null;
    }

    public String getSetterName() {
        return setterName;
    }

    public String getGetterName() {
        return getterName;
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

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public boolean hasSetter() {
        return setterName != null && !setterName.isBlank();
    }

    public boolean hasGetter() {
        return getterName != null && !getterName.isBlank();
    }
}
