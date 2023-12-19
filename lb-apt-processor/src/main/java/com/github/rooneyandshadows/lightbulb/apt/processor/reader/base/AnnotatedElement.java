package com.github.rooneyandshadows.lightbulb.apt.processor.reader.base;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;

public final class AnnotatedElement {
    private final Element element;
    private final Annotation annotation;

    public AnnotatedElement(Element element, Annotation annotation) {
        this.element = element;
        this.annotation = annotation;
    }

    public Element getElement() {
        return element;
    }

    public Annotation getAnnotation() {
        return annotation;
    }
}