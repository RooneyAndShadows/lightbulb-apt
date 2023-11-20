package com.github.rooneyandshadows.lightbulb.annotation_processors.data.activity;

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.ActivityConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotatedElement;
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.ClassNames;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.List;

public class ActivityBindingData {
    private final TypeMirror type;
    private final ClassName className;
    private boolean routingEnabled;

    public ActivityBindingData(Elements elements, TypeElement activityClassElement, List<AnnotatedElement> annotatedElements) {
        this.type = activityClassElement.asType();
        this.className = ClassNames.generateClassName(activityClassElement, elements);
        annotatedElements.forEach(this::handleActivityConfiguration);
    }

    public TypeMirror getType() {
        return type;
    }

    public boolean isRoutingEnabled() {
        return routingEnabled;
    }

    public ClassName getClassName() {
        return className;
    }

    private void handleActivityConfiguration(AnnotatedElement element) {
        Annotation annotation = element.getAnnotation();
        if (!(annotation instanceof ActivityConfiguration config)) return;
        routingEnabled = config.enableRouterGeneration();
    }
}