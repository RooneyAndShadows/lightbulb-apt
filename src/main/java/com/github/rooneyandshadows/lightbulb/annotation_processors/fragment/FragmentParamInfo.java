package com.github.rooneyandshadows.lightbulb.annotation_processors.fragment;

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentParameter;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils.getTypeOfFieldElement;

@SuppressWarnings("DuplicatedCode")
public class FragmentParamInfo extends FragmentVariableInfo {
    private final boolean optional;

    public FragmentParamInfo(Element element, FragmentParameter annotation) {
        super(element);
        this.optional = annotation.optional();
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