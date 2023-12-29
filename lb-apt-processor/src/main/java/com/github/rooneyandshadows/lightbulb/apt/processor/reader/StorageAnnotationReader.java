package com.github.rooneyandshadows.lightbulb.apt.processor.reader;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.LightbulbStorage;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.StorageMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationReader;
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

import static com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.StorageMetadata.*;
import static com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry.AnnotationResultTypes.LIGHTBULB_STORAGE_DESCRIPTION;

public class StorageAnnotationReader extends AnnotationReader {
    private final List<StorageMetadata> storageMetadataList = new ArrayList<>();

    public StorageAnnotationReader(AnnotationResultsRegistry resultsRegistry, Messager messager, Elements elements, RoundEnvironment environment) {
        super(resultsRegistry, messager, elements, environment);
    }

    @Override
    protected void handleAnnotationsForClass(TypeElement target, List<AnnotatedElement> annotatedElements) {
        String name = null;
        String[] subkeys = null;
        List<TargetField> targetFields = ElementUtils.getFieldElements(target)
                .stream()
                .map(TargetField::new)
                .toList();

        for (AnnotatedElement element : annotatedElements) {
            Annotation annotation = element.getAnnotation();
            if (annotation instanceof LightbulbStorage lightbulbStorage) {
                name = lightbulbStorage.name();
                subkeys = lightbulbStorage.subKeys();
            }
        }

        StorageMetadata metadata = new StorageMetadata(target, name, subkeys, targetFields);

        storageMetadataList.add(metadata);
    }

    @Override
    protected void onAnnotationsExtracted(AnnotationResultsRegistry resultRegistry) {
        resultRegistry.setResult(LIGHTBULB_STORAGE_DESCRIPTION, storageMetadataList);
    }

    @Override
    protected Map<Class<? extends Annotation>, ElementKind> getAnnotationTargets() {
        Map<Class<? extends Annotation>, ElementKind> targets = new HashMap<>();
        targets.put(LightbulbStorage.class, ElementKind.CLASS);
        return targets;
    }
}
