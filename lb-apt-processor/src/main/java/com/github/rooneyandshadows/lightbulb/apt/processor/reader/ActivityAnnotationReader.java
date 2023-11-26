package com.github.rooneyandshadows.lightbulb.apt.processor.reader;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbActivity;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.activity.ActivityBindingData;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationReader;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityAnnotationReader extends AnnotationReader {
    private final List<ActivityBindingData> activityBindings = new ArrayList<>();

    public ActivityAnnotationReader(AnnotationResultsRegistry resultsRegistry, Messager messager, Elements elements, RoundEnvironment environment) {
        super(resultsRegistry, messager, elements, environment);
    }

    @Override
    protected void onAnnotationsExtracted(Map<Element, List<AnnotatedElement>> annotations, AnnotationResultsRegistry resultRegistry) {
        for (Map.Entry<Element, List<AnnotatedElement>> entry : annotations.entrySet()) {
            TypeElement activityClassElement = (TypeElement) entry.getKey();
            List<AnnotatedElement> annotatedElements = entry.getValue();

            ActivityBindingData bindingData = new ActivityBindingData(elements, activityClassElement, annotatedElements);
            activityBindings.add(bindingData);
        }
        resultsRegistry.setResult(AnnotationResultsRegistry.AnnotationResultTypes.ACTIVITY_BINDINGS, activityBindings);
    }

    @Override
    protected Map<Class<? extends Annotation>, ElementKind> getAnnotationTargets() {
        Map<Class<? extends Annotation>, ElementKind> targets = new HashMap<>();
        targets.put(LightbulbActivity.class, ElementKind.CLASS);
        return targets;
    }
}
