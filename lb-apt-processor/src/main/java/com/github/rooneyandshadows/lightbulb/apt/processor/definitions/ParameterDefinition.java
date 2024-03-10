package com.github.rooneyandshadows.lightbulb.apt.processor.definitions;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.base.BaseDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;

import javax.lang.model.element.VariableElement;

public class ParameterDefinition extends BaseDefinition<VariableElement> {
    private final String name;
    private final TypeDefinition type;
    private final boolean nullable;

    public ParameterDefinition(VariableElement element) {
        super(element);
        this.name = element.getSimpleName().toString();
        this.type = new TypeDefinition(element.asType());
        this.nullable = ElementUtils.isNullable(element);
    }

    public String getName() {
        return name;
    }

    public TypeDefinition getType() {
        return type;
    }

    public boolean isNullable() {
        return nullable;
    }
}