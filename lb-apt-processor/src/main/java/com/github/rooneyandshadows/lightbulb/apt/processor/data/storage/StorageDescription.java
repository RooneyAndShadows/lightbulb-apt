package com.github.rooneyandshadows.lightbulb.apt.processor.data.storage;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbStorage;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.List;

public class StorageDescription {
    private final TypeMirror type;
    private final ClassName className;
    private String name;

    public StorageDescription(Elements elements, TypeElement activityClassElement, List<AnnotatedElement> annotatedElements) {
        this.type = activityClassElement.asType();
        this.className = ClassNames.generateClassName(activityClassElement, elements);
        annotatedElements.forEach(this::handleActivityConfiguration);
    }

    public ClassName getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    private void handleActivityConfiguration(AnnotatedElement element) {
        Annotation annotation = element.getAnnotation();
        if (!(annotation instanceof LightbulbStorage storage)) return;
        name = storage.name();
    }
}