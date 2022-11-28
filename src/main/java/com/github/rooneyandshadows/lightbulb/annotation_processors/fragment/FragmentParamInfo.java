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

    public String getName() {
        return name;
    }

    public TypeName getType() {
        return type;
    }

    public boolean isOptional() {
        return optional;
    }

    public String getSetterName() {
        return setterName;
    }

    public String getGetterName() {
        return getterName;
    }

    public boolean hasSetter() {
        return hasSetter;
    }

    public boolean hasGetter() {
        return hasGetter;
    }

    public boolean isNullable() {
        return isNullable;
    }

    private boolean scanParentForSetter() {
        Element parent = element.getEnclosingElement();
        if (parent.getKind() != ElementKind.CLASS)
            return false;
        return parent.getEnclosedElements()
                .stream().anyMatch(target -> {
                    String targetName = target.getSimpleName().toString();
                    boolean take = target.getKind() == ElementKind.METHOD;
                    take &= targetName.equals(setterName);
                    return take;
                });
    }

    private boolean scanParentForGetter() {
        Element parent = element.getEnclosingElement();
        if (parent.getKind() != ElementKind.CLASS)
            return false;
        return parent.getEnclosedElements()
                .stream().anyMatch(target -> {
                    String targetName = target.getSimpleName().toString();
                    boolean take = target.getKind() == ElementKind.METHOD;
                    take &= targetName.equals(getterName);
                    return take;
                });
    }

    private String capitalizeName() {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}