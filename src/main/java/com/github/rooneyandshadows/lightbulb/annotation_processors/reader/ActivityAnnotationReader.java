package com.github.rooneyandshadows.lightbulb.annotation_processors.reader;

import com.github.rooneyandshadows.lightbulb.annotation_processors.activity.ActivityInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.activity.ActivityConfiguration;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationReader;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityAnnotationReader extends AnnotationReader {
    private final List<ActivityInfo> activityInfoList = new ArrayList<>();

    public ActivityAnnotationReader(Messager messager, Elements elements, RoundEnvironment environment) {
        super(messager, elements, environment);
    }

    public List<ActivityInfo> getActivityInfoList() {
        return activityInfoList;
    }

    @Override
    protected void onAnnotationsExtracted(Map<String, List<AnnotatedElement>> annotatedElements) {
        for (Map.Entry<String, List<AnnotatedElement>> entry : annotatedElements.entrySet()) {
            String className = entry.getKey();
            List<AnnotatedElement> annotatedTargets = entry.getValue();
            annotatedTargets.forEach(annotationTarget -> {

            });
        }
    }

    @Override
    protected Map<Class<? extends Annotation>, ElementKind> getAnnotationTargets() {
        Map<Class<? extends Annotation>, ElementKind> targets = new HashMap<>();
        targets.put(ActivityConfiguration.class, ElementKind.CLASS);
        return targets;
    }
}
