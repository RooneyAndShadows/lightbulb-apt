package com.github.rooneyandshadows.lightbulb.apt.processor.reader;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.LightbulbParcelable;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.ParcelableMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.ParcelableMetadata.TargetField;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry;
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

import static com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry.AnnotationResultTypes.LIGHTBULB_PARCELABLE_DESCRIPTION;

public class ParcelableAnnotationReader extends AnnotationReader {
    private final List<ParcelableMetadata> parcelableMetadataList = new ArrayList<>();

    public ParcelableAnnotationReader(AnnotationResultsRegistry resultsRegistry, Messager messager, Elements elements, RoundEnvironment environment) {
        super(resultsRegistry, messager, elements, environment);
    }

    @Override
    protected void handleAnnotationsForClass(TypeElement target, List<AnnotatedElement> annotatedElements) {
        List<TargetField> targetFields = ElementUtils.getFieldElements(target)
                .stream()
                .map(TargetField::new)
                .toList();

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
