package com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities;

import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.TypeDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.base.DeclaredValueHolder;

public class Variable extends DeclaredValueHolder {
    public Variable(String name, TypeDefinition typeInformation) {
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
}