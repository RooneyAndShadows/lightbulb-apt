package com.github.rooneyandshadows.lightbulb.apt.processor.definitions;

import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.*;
import java.util.List;


public final class MethodDefinition {
    private final boolean isFinal;
    private final TypeDefinition returnType;
    private final List<Parameter> parameters;

    public MethodDefinition(ExecutableElement methodElement) {
        this.isFinal = ElementUtils.isFinal(methodElement);
        this.returnType = new TypeDefinition(methodElement.getReturnType());
        this.parameters = Parameter.from(methodElement);
    }

    public static class Parameter {
        private final String name;
        private final TypeDefinition type;
        private final boolean nullable;

        public Parameter(VariableElement element) {
            this.name = element.getSimpleName().toString();
            this.type = new TypeDefinition(element.asType());
            this.nullable = element.getAnnotation(Nullable.class) != null;
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

        private static List<Parameter> from(ExecutableElement methodElement) {
            return methodElement.getParameters().stream().map(Parameter::new).toList();
        }
    }
}