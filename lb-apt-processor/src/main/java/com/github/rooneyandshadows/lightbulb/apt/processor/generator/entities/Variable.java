package com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.FragmentMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.base.DeclaredValueHolder;

import javax.lang.model.type.TypeMirror;

public class Variable extends DeclaredValueHolder {
    public Variable(String name, TypeMirror typeMirror) {
        super(name, typeMirror);
    }

    @Override
    public String getValueAccessor() {
        return name;
    }

    @Override
    public String getValueSetStatement(String setVarName) {
        return String.format("%s = %s", name, setVarName);
    }

    public static Variable from(FragmentMetadata.Parameter parameter) {
        String name = parameter.element().getSimpleName().toString();
        TypeMirror typeMirror = parameter.element().asType();

        return new Variable(name, typeMirror);
    }
}