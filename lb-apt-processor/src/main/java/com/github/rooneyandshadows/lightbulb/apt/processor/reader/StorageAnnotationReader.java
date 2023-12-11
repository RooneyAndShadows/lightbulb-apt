package com.github.rooneyandshadows.lightbulb.apt.processor.reader;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.LightbulbStorageDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.common.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationReader;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;

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

import static com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry.AnnotationResultTypes.LIGHTBULB_STORAGE_DESCRIPTION;

public class StorageAnnotationReader extends AnnotationReader {
    private final List<LightbulbStorageDescription> storageDescriptions = new ArrayList<>();

    public StorageAnnotationReader(AnnotationResultsRegistry resultsRegistry, Messager messager, Elements elements, RoundEnvironment environment) {
        super(resultsRegistry, messager, elements, environment);
    }

    @Override
    protected void handleAnnotationsForClass(TypeElement target, List<AnnotatedElement> annotatedElements) {
        LightbulbStorageDescription.Builder storageDescriptionBuilder = new LightbulbStorageDescription.Builder(elements, target);

        annotatedElements.forEach(element -> {
            Annotation annotation = element.getAnnotation();
            if (annotation instanceof LightbulbStorage lightbulbStorage) {
                List<Field> fields = ElementUtils.getFieldElements((TypeElement) element.getElement())
                        .stream()
                        .map(Field::new)
                        .toList();
                storageDescriptionBuilder.withName(lightbulbStorage.name());
                storageDescriptionBuilder.withSubKeys(lightbulbStorage.subKeys());
                storageDescriptionBuilder.withFields(fields);
            }
        });

        storageDescriptions.add(storageDescriptionBuilder.build());
    }

    @Override
    protected void onAnnotationsExtracted(AnnotationResultsRegistry resultRegistry) {
        resultRegistry.setResult(LIGHTBULB_STORAGE_DESCRIPTION, storageDescriptions);
    }

    @Override
    protected Map<Class<? extends Annotation>, ElementKind> getAnnotationTargets() {
        Map<Class<? extends Annotation>, ElementKind> targets = new HashMap<>();
        targets.put(LightbulbStorage.class, ElementKind.CLASS);
        return targets;
    }
}
