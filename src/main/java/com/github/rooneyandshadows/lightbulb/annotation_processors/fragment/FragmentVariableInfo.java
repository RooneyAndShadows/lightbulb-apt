package com.github.rooneyandshadows.lightbulb.annotation_processors.fragment;

import com.squareup.javapoet.TypeName;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils.getTypeOfFieldElement;

@SuppressWarnings("DuplicatedCode")
public class FragmentVariableInfo {
    protected final String name;
    protected final TypeName type;
    protected final String setterName;
    protected final String getterName;
    protected final Element element;
    protected final boolean hasSetter;
    protected final boolean hasGetter;
    protected final boolean isNullable;

    public FragmentVariableInfo(Element fieldElement) {
        this.element = fieldElement;
        this.name = fieldElement.getSimpleName().toString();
        this.type = getTypeOfFieldElement(fieldElement);
        this.setterName = "set".concat(capitalizeName());
        this.getterName = "get".concat(capitalizeName());
        this.hasSetter = scanParentForSetter();
        this.hasGetter = scanParentForGetter();
        this.isNullable = fieldElement.getAnnotation(Nullable.class) != null;
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
        if (parent.getKind() != ElementKind.CLASS) return false;
        return parent.getEnclosedElements().stream().anyMatch(target -> {
            String targetName = target.getSimpleName().toString();
            boolean take = target.getKind() == ElementKind.METHOD;
            take &= targetName.equals(setterName);
            return take;
        });
    }

    private boolean scanParentForGetter() {
        Element parent = element.getEnclosingElement();
        if (parent.getKind() != ElementKind.CLASS) return false;
        return parent.getEnclosedElements().stream().anyMatch(target -> {
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