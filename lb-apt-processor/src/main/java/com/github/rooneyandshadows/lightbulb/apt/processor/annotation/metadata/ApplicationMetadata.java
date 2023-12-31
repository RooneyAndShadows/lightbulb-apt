package com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.ClassMetadata;

import javax.lang.model.element.TypeElement;

public final class ApplicationMetadata extends ClassMetadata {

    public ApplicationMetadata(TypeElement element) {
        super(element);
    }
}