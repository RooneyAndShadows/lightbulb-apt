package com.github.rooneyandshadows.lightbulb.apt.processor.reader;

import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbActivity;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.ActivityMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.ClassDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationReader;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry.AnnotationResultTypes.LIGHTBULB_ACTIVITY_DESCRIPTION;

public class ActivityAnnotationReader extends AnnotationReader {
    private final List<ActivityMetadata> activityMetadataList = new ArrayList<>();

    public ActivityAnnotationReader(AnnotationResultsRegistry resultsRegistry, Messager messager, Elements elements, RoundEnvironment environment) {
        super(resultsRegistry, messager, elements, environment);
    }

    @Override
    protected void handleAnnotationsForClass(TypeElement target, List<AnnotatedElement> annotatedElements) {
        ClassDefinition activityClassDefinition = new ClassDefinition(target);
        ActivityMetadata metadata = new ActivityMetadata(activityClassDefinition, annotatedElements);

        activityMetadataList.add(metadata);
    }

    @Override
    protected void onAnnotationsExtracted(AnnotationResultsRegistry resultRegistry) {
        resultRegistry.setResult(LIGHTBULB_ACTIVITY_DESCRIPTION, activityMetadataList);
    }

    @Override
    protected Map<Class<? extends Annotation>, ElementKind> getAnnotationTargets() {
        Map<Class<? extends Annotation>, ElementKind> targets = new HashMap<>();
        targets.put(LightbulbActivity.class, ElementKind.CLASS);
        return targets;
    }
}
