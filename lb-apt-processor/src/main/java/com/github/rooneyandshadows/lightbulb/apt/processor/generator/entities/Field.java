package com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.FragmentMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.ParcelableMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.ParcelableMetadata.TargetField;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.StorageMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.BaseMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.base.DeclaredEntity;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.MemberUtils;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.stream.Collectors;


@SuppressWarnings("DuplicatedCode")
public class Field extends DeclaredEntity {
    protected final Element element;
    protected final Element enclosingClassElement;
    protected final String setterName;
    protected final String getterName;
    protected final Modifier accessModifier;
    protected final Modifier setterAccessModifier;
    protected final Modifier getterAccessModifier;
    protected final boolean isFinal;
    protected final boolean isNullable;
    protected final boolean hasSetter;
    protected final boolean hasGetter;

    public Field(Element fieldElement) {
        super(ElementUtils.getSimpleName(fieldElement), fieldElement.asType());
        this.enclosingClassElement = fieldElement.getEnclosingElement();
        this.element = fieldElement;
        this.setterName = MemberUtils.getFieldSetterName(name);
        this.getterName = MemberUtils.getFieldGetterName(name);
        this.accessModifier = ElementUtils.getAccessModifier(fieldElement);
        this.setterAccessModifier = ElementUtils.getMethodAccessModifier(enclosingClassElement, setterName);
        this.getterAccessModifier = ElementUtils.getMethodAccessModifier(enclosingClassElement, getterName);
        this.isFinal = ElementUtils.isFinal(fieldElement);
        this.isNullable = fieldElement.getAnnotation(Nullable.class) != null;
        this.hasSetter = ElementUtils.scanForSetter(enclosingClassElement, name);
        this.hasGetter = ElementUtils.scanForGetter(enclosingClassElement, name);
    }

    public static Field from(FragmentMetadata.Parameter fragmentParameter) {
        return new Field(fragmentParameter.element());
    }

    public static Field from(FragmentMetadata.ViewBinding viewBinding) {
        return new Field(viewBinding.element());
    }

    public static Field from(FragmentMetadata.StatePersisted statePersisted) {
        return new Field(statePersisted.element());
    }

    public static Field from(ParcelableMetadata.TargetField targetField) {
        return new Field(targetField.element());
    }

    public static Field from(StorageMetadata.TargetField targetField) {
        return new Field(targetField.element());
    }

    public String getSetterName() {
        return setterName;
    }

    public String getGetterName() {
        return getterName;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean hasSetter() {
        return hasSetter;
    }

    public boolean hasGetter() {
        return hasGetter;
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

    public boolean accessModifierAtLeast(Modifier target) {

        if (target == null || accessModifier == null) {
            return false;
        }

        return accessModifier.ordinal() <= target.ordinal();
    }
}