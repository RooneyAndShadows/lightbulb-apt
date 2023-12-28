package com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.BaseMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.base.DeclaredEntity;

import javax.lang.model.type.TypeMirror;

public class Variable extends DeclaredEntity {
    public Variable(String name, TypeMirror typeMirror, BaseMetadata metadata) {
        super(name, typeMirror, metadata);
    }

    public Variable(String name, TypeMirror typeMirror) {
        super(name, typeMirror);
    }
}