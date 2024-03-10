package com.github.rooneyandshadows.lightbulb.apt.processor.definitions.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;

import javax.lang.model.AnnotatedConstruct;
import java.lang.annotation.Annotation;

public class BaseDefinition<T extends AnnotatedConstruct> {
    protected final T construct;

    public BaseDefinition(T construct) {
        this.construct = construct;
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotationTarget) {
        return ElementUtils.hasAnnotation(construct, annotationTarget);
    }
}
