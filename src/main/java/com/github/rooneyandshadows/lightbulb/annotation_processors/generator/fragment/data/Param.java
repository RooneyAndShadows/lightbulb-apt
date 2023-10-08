package com.github.rooneyandshadows.lightbulb.annotation_processors.generator.fragment.data;

import com.squareup.javapoet.ParameterSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;

@SuppressWarnings("DuplicatedCode")
public class Param extends Variable {
    private final boolean optional;

    public Param(Element element, boolean optional) {
        super(element);
        this.optional = optional;
    }

    public ParameterSpec getParameterSpec() {
        ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(type, name);
        if (isNullable || isOptional())
            parameterBuilder.addAnnotation(Nullable.class);
        else
            parameterBuilder.addAnnotation(NotNull.class);
        return parameterBuilder.build();
    }

    public boolean isOptional() {
        return optional;
    }
}