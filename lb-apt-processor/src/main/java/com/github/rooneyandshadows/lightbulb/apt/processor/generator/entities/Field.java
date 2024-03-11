package com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.FieldMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.base.DeclaredValueHolder;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.TypeDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings("DuplicatedCode")
public class Field extends DeclaredValueHolder {
    private final String setter;
    private final String getter;

    public Field(String name, TypeDefinition typeInformation, @Nullable String setterName, @Nullable String getterName) {
        super(name, typeInformation);
        this.setter = setterName;
        this.getter = getterName;
    }

    @Override
    public String getValueAccessor() {
        if (getter == null || getter.isBlank()) {
            return String.format("this.%s", name);
        } else {
            return String.format("this.%s()", getter);
        }
    }

    @Override
    public String getValueSetStatement(String setVarName) {
        if (setter == null || setter.isBlank()) {
            return String.format("this.%s = %s", name, setVarName);
        } else {
            return String.format("this.%s(%s)", setter, setVarName);
        }
    }

    @NotNull
    public static Field from(FieldMetadata fieldMetadata) {
        return new Field(
                fieldMetadata.getName(),
                fieldMetadata.getType(),
                fieldMetadata.getSetterName(),
                fieldMetadata.getGetterName()
        );
    }
}