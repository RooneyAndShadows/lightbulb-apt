package com.github.rooneyandshadows.lightbulb.apt.processor.reader.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.common.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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