package com.github.rooneyandshadows.lightbulb.apt.processor.definitions;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.base.ElementDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;

import javax.lang.model.element.ExecutableElement;
import java.util.List;


public final class MethodDefinition extends ElementDefinition<ExecutableElement> {
    private final boolean isFinal;
    private final TypeDefinition returnType;
    private final List<ParameterDefinition> parameters;

    public MethodDefinition(ExecutableElement methodElement) {
        super(methodElement);
        this.isFinal = ElementUtils.isFinal(methodElement);
        this.returnType = new TypeDefinition(methodElement.getReturnType());
        this.parameters = methodElement.getParameters().stream().map(ParameterDefinition::new).toList();
    }

    public boolean isFinal() {
        return isFinal;
    }

    public TypeDefinition getReturnType() {
        return returnType;
    }

    public List<ParameterDefinition> getParameters(boolean includeNullable) {
        return parameters.stream().filter(parameterDefinition -> includeNullable || !parameterDefinition.isNullable()).toList();
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    public boolean hasNullableParameters() {
        return parameters.stream().anyMatch(ParameterDefinition::isNullable);
    }

    public static List<MethodDefinition> from(List<ExecutableElement> methods) {
        return methods.stream().map(MethodDefinition::new).toList();
    }
}