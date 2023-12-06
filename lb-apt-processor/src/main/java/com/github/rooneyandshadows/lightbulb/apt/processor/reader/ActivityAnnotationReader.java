package com.github.rooneyandshadows.lightbulb.apt.processor.reader;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.LightbulbActivityDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
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

import static com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry.AnnotationResultTypes.LIGHTBULB_ACTIVITY_DESCRIPTION;

public class ActivityAnnotationReader extends AnnotationReader {
    private final List<LightbulbActivityDescription> activityBindings = new ArrayList<>();

    public ActivityAnnotationReader(AnnotationResultsRegistry resultsRegistry, Messager messager, Elements elements, RoundEnvironment environment) {
        super(resultsRegistry, messager, elements, environment);
    }

    @Override
    protected void handleAnnotationsForClass(TypeElement target, List<AnnotatedElement> annotatedElements) {
        LightbulbActivityDescription.Builder activityDataBuilder = new LightbulbActivityDescription.Builder(elements, target);

        annotatedElements.forEach(element -> {
            consumeAnnotation(LightbulbActivity.class, element, lightbulbActivity -> {
                activityDataBuilder.withRoutingEnabled(lightbulbActivity.enableRouterGeneration());
                activityDataBuilder.withFragmentContainerId(lightbulbActivity.fragmentContainerId());
            });
        });

        activityBindings.add(activityDataBuilder.build());
    }

    @Override
    protected void onAnnotationsExtracted(AnnotationResultsRegistry resultRegistry) {
        resultRegistry.setResult(LIGHTBULB_ACTIVITY_DESCRIPTION, activityBindings);
    }

    @Override
    protected Map<Class<? extends Annotation>, ElementKind> getAnnotationTargets() {
        Map<Class<? extends Annotation>, ElementKind> targets = new HashMap<>();
        targets.put(LightbulbActivity.class, ElementKind.CLASS);
        return targets;
    }
}
