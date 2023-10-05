package com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils.getFullClassName;
import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils.getSimpleName;

public class AnnotationReader {
    protected final Messager messager;
    protected final Elements elements;
    protected final RoundEnvironment environment;

    public AnnotationReader(Messager messager, Elements elements, RoundEnvironment environment) {
        this.messager = messager;
        this.elements = elements;
        this.environment = environment;
    }

    protected <T extends Annotation> boolean obtainAnnotations(Class<T> annotationClass, ElementKind requiredKind, AnnotationConsumer<T> targetConsumer) {
        for (Element element : environment.getElementsAnnotatedWith(annotationClass)) {
            if (element.getKind() != requiredKind) {
                String annotationClassName = annotationClass.getSimpleName();
                String errorString = String.format("Wrong annotation target for @%s", annotationClassName);
                messager.printMessage(Diagnostic.Kind.ERROR, errorString);
                return false;
            }
            Element enclosingClassElement = requiredKind.isField() ? element.getEnclosingElement() : null;
            String className = getFullClassName(elements, enclosingClassElement != null ? enclosingClassElement : element);
            String targetElementSimpleName = getSimpleName(elements, element);
            T annotation = element.getAnnotation(annotationClass);
            targetConsumer.accept(targetElementSimpleName, className, annotation);
        }
        return true;
    }

    protected interface AnnotationConsumer<T extends Annotation> {
        void accept(String targetElementSimpleName, String enclosingClassName, T annotation);
    }
}
