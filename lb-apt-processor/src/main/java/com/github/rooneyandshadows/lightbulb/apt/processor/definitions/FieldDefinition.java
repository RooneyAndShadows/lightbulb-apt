package com.github.rooneyandshadows.lightbulb.apt.processor.definitions;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.base.BaseDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;


public final class FieldDefinition extends BaseDefinition<VariableElement> {
    private final String name;
    private final String setterName;
    private final String getterName;
    private final Modifier accessModifier;
    private final Modifier setterAccessModifier;
    private final Modifier getterAccessModifier;
    private final TypeDefinition type;
    private final boolean isFinal;
    private final boolean isNullable;

    public FieldDefinition(VariableElement element) {
        super(element);
        TypeElement enclosingClassElement = (TypeElement) element.getEnclosingElement();
        this.name = element.getSimpleName().toString();
        this.setterName = ElementUtils.findFieldSetterName(enclosingClassElement, name);
        this.getterName = ElementUtils.findFieldGetterName(enclosingClassElement, name);
        this.accessModifier = ElementUtils.getAccessModifier(element);
        this.setterAccessModifier = ElementUtils.getMethodAccessModifier(enclosingClassElement, setterName);
        this.getterAccessModifier = ElementUtils.getMethodAccessModifier(enclosingClassElement, getterName);
        this.type = new TypeDefinition(element.asType());
        this.isFinal = ElementUtils.isFinal(element);
        this.isNullable = ElementUtils.isNullable(element);
    }

    public String getName() {
        return name;
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

    public TypeDefinition getType() {
        return type;
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

    public static List<FieldDefinition> from(List<VariableElement> fields) {
        return fields.stream().map(FieldDefinition::new).toList();
    }
}