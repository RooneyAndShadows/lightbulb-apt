package com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base;


import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.*;

public abstract class AnnotationReader {
    protected final Messager messager;
    protected final Elements elements;
    protected final RoundEnvironment environment;
    protected final AnnotationResultsRegistry resultsRegistry;
    private final Map<Class<? extends Annotation>, ElementKind> targets;

    public AnnotationReader(AnnotationResultsRegistry resultsRegistry, Messager messager, Elements elements, RoundEnvironment environment) {
        this.resultsRegistry = resultsRegistry;
        this.messager = messager;
        this.elements = elements;
        this.environment = environment;
        this.targets = Collections.unmodifiableMap(getAnnotationTargets());
    }

    protected abstract void onAnnotationsExtracted(Map<Element, List<AnnotatedElement>> annotations, AnnotationResultsRegistry resultRegistry);

    protected abstract Map<Class<? extends Annotation>, ElementKind> getAnnotationTargets();

    public final boolean readAnnotations() {
        Map<Element, List<AnnotatedElement>> annotatedElements = new HashMap<>();
        for (Map.Entry<Class<? extends Annotation>, ElementKind> entry : targets.entrySet()) {
            Class<? extends Annotation> annotationClass = entry.getKey();
            ElementKind elementKind = entry.getValue();
            boolean result = obtainAnnotations(annotationClass, elementKind, annotatedElements);
            if (!result) {
                return false;
            }
        }
        onAnnotationsExtracted(annotatedElements, resultsRegistry);
        return true;
    }

    private <T extends Annotation> boolean obtainAnnotations(Class<T> annotationClass, ElementKind requiredKind, Map<Element, List<AnnotatedElement>> annotatedElements) {
        for (Element element : environment.getElementsAnnotatedWith(annotationClass)) {
            if (element.getKind() != requiredKind) {
                String annotationClassName = annotationClass.getSimpleName();
                String errorString = String.format("Wrong annotation target for @%s", annotationClassName);
                messager.printMessage(Diagnostic.Kind.ERROR, errorString);
                return false;
            }

            T annotation = element.getAnnotation(annotationClass);
            Element key = element.getKind() == ElementKind.CLASS ? element : element.getEnclosingElement();
            List<AnnotatedElement> targetsForClassName = annotatedElements.computeIfAbsent(key, targetKey -> new ArrayList<>());
            AnnotatedElement annotationTarget = new AnnotatedElement(element, annotation);
            targetsForClassName.add(annotationTarget);
        }
        return true;
    }
}