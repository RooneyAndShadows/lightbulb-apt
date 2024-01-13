package com.github.rooneyandshadows.lightbulb.apt.processor.reader;

import com.github.rooneyandshadows.lightbulb.apt.annotations.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.ParcelableMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.ParcelableMetadata.TargetField;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationReader;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry.AnnotationResultTypes.LIGHTBULB_PARCELABLE_DESCRIPTION;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils.*;

public class ParcelableAnnotationReader extends AnnotationReader {
    private final List<ParcelableMetadata> parcelableMetadataList = new ArrayList<>();

    public ParcelableAnnotationReader(AnnotationResultsRegistry resultsRegistry, Messager messager, Elements elements, RoundEnvironment environment) {
        super(resultsRegistry, messager, elements, environment);
    }

    @Override
    protected void handleAnnotationsForClass(TypeElement target, List<AnnotatedElement> annotatedElements) {
        List<TargetField> targetFields = new ArrayList<>();

        getFieldElements(target).forEach(element -> {
            targetFields.add(new TargetField((VariableElement) element));
        });

        ParcelableMetadata metadata = new ParcelableMetadata(target, targetFields);

        parcelableMetadataList.add(metadata);
    }

    @Override
    protected void onAnnotationsExtracted(AnnotationResultsRegistry resultRegistry) {
        resultRegistry.setResult(LIGHTBULB_PARCELABLE_DESCRIPTION, parcelableMetadataList);
    }

    @Override
    protected Map<Class<? extends Annotation>, ElementKind> getAnnotationTargets() {
        Map<Class<? extends Annotation>, ElementKind> targets = new HashMap<>();
        targets.put(LightbulbParcelable.class, ElementKind.CLASS);
        return targets;
    }
}
