package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata;

import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbActivity;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.ClassMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.ClassDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;

import java.lang.annotation.Annotation;
import java.util.List;

public final class ActivityMetadata extends ClassMetadata {
    private String layoutName;
    private String fragmentContainerId;

    public ActivityMetadata(ClassDefinition activityClassDefinition, List<AnnotatedElement> annotatedElements) {
        super(activityClassDefinition);
        extractValues(annotatedElements);
    }

    public String getFragmentContainerId() {
        return fragmentContainerId;
    }

    public String getLayoutName() {
        return layoutName;
    }

    private void extractValues(List<AnnotatedElement> annotatedElements) {
        for (AnnotatedElement element : annotatedElements) {
            Annotation annotation = element.getAnnotation();
            if (annotation instanceof LightbulbActivity lightbulbActivity) {
                fragmentContainerId = lightbulbActivity.fragmentContainerId();
                layoutName = lightbulbActivity.layoutName();
            }
        }
    }
}