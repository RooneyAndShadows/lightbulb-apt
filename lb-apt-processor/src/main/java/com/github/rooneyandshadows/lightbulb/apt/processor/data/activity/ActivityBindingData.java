package com.github.rooneyandshadows.lightbulb.apt.processor.data.activity;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.ActivityConfiguration;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.GENERATED_CLASS_NAME_PREFIX;

public class ActivityBindingData {
    private final TypeMirror type;
    private final ClassName className;
    private final ClassName superClassName;
    private final ClassName instrumentedClassName;
    private boolean routingEnabled;

    public ActivityBindingData(Elements elements, TypeElement activityClassElement, List<AnnotatedElement> annotatedElements) {
        this.type = activityClassElement.asType();
        this.className = ClassNames.generateClassName(activityClassElement, elements);
        this.superClassName = ClassNames.generateSuperClassName(activityClassElement, elements);
        this.instrumentedClassName = ClassNames.generateClassNameWithPrefix(PackageNames.getActivitiesPackage(), className.simpleName(), GENERATED_CLASS_NAME_PREFIX);
        annotatedElements.forEach(this::handleActivityConfiguration);
    }

    public ClassName getClassName() {
        return className;
    }

    public ClassName getSuperClassName() {
        return superClassName;
    }

    public ClassName getInstrumentedClassName() {
        return instrumentedClassName;
    }

    public boolean isRoutingEnabled() {
        return routingEnabled;
    }

    private void handleActivityConfiguration(AnnotatedElement element) {
        Annotation annotation = element.getAnnotation();
        if (!(annotation instanceof ActivityConfiguration config)) return;
        routingEnabled = config.enableRouterGeneration();
    }
}