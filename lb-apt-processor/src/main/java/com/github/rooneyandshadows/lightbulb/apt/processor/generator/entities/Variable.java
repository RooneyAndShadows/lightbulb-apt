package com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities;

import com.github.rooneyandshadows.lightbulb.apt.processor.TypeInformation;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.FieldMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.base.DeclaredValueHolder;
import org.jetbrains.annotations.NotNull;

public class Variable extends DeclaredValueHolder {
    public Variable(String name, TypeInformation typeInformation) {
        super(name, typeInformation);
    }

    @Override
    public String getValueAccessor() {
        return name;
    }

    @Override
    public String getValueSetStatement(String setVarName) {
        return String.format("%s = %s", name, setVarName);
    }

    @NotNull
    public static Variable from(FieldMetadata fieldMetadata) {
        return new Variable(
                fieldMetadata.getName(),
                fieldMetadata.getTypeInformation()
        );
    }
}