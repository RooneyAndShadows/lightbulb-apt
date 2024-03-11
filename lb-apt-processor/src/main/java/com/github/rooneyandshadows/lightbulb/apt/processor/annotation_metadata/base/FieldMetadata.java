package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.FieldDefinition;

import javax.lang.model.element.Modifier;


public abstract class FieldMetadata extends TypedMetadata {
    protected final FieldDefinition fieldDefinition;

    public FieldMetadata(FieldDefinition fieldDefinition) {
        super(fieldDefinition.getType());
        this.fieldDefinition = fieldDefinition;
    }

    public boolean hasSetter() {
        return fieldDefinition.hasSetter();
    }

    public boolean hasGetter() {
        return fieldDefinition.hasGetter();
    }

    public String getGetterName() {
        return fieldDefinition.getGetterName();
    }

    public String getSetterName() {
        return fieldDefinition.getSetterName();
    }

    public Modifier getGetterAccessModifier() {
        return fieldDefinition.getGetterAccessModifier();
    }

    public Modifier getSetterAccessModifier() {
        return fieldDefinition.getSetterAccessModifier();
    }

    public Modifier getAccessModifier() {
        return fieldDefinition.getAccessModifier();
    }

    public boolean isFinal() {
        return fieldDefinition.isFinal();
    }

    public boolean isNullable() {
        return fieldDefinition.isNullable();
    }

    public String getName() {
        return fieldDefinition.getName();
    }
}
