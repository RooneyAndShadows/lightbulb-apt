package com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.inner;

import com.squareup.javapoet.ParameterSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;

@SuppressWarnings("DuplicatedCode")
public class Parameter extends Variable {
    private final boolean optional;

    public Parameter(Element element, boolean optional) {
        super(element);
        this.optional = optional;
    }

    public boolean isOptional() {
        return optional;
    }
}