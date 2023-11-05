package com.github.rooneyandshadows.lightbulb.annotation_processors.reader;

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.ActivityConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.data.activity.ActivityBindingData;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotatedElement;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationReader;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry.AnnotationResultTypes.ACTIVITY_BINDINGS;

public class ActivityAnnotationReader extends AnnotationReader {
    private final List<ActivityBindingData> activityBindings = new ArrayList<>();

    public ActivityAnnotationReader(AnnotationResultsRegistry resultsRegistry, Messager messager, Elements elements, RoundEnvironment environment) {
        super(resultsRegistry, messager, elements, environment);
    }

    @Override
    protected void onAnnotationsExtracted(Map<Element, List<AnnotatedElement>> annotations, AnnotationResultsRegistry resultRegistry) {
        for (Map.Entry<Element, List<AnnotatedElement>> entry : annotations.entrySet()) {
            Element activityClassElement = entry.getKey();
            List<AnnotatedElement> annotatedElements = entry.getValue();

            ActivityBindingData bindingData = new ActivityBindingData(elements, activityClassElement, annotatedElements);
            activityBindings.add(bindingData);
        }
        resultsRegistry.setResult(ACTIVITY_BINDINGS, activityBindings);
    }

    @Override
    protected Map<Class<? extends Annotation>, ElementKind> getAnnotationTargets() {
        Map<Class<? extends Annotation>, ElementKind> targets = new HashMap<>();
        targets.put(ActivityConfiguration.class, ElementKind.CLASS);
        return targets;
    }
}