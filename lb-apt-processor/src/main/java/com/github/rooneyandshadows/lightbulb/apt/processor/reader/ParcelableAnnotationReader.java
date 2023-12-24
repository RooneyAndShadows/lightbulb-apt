package com.github.rooneyandshadows.lightbulb.apt.processor.reader;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotations.LightbulbParcelable;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.LightbulbParcelableDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.Field;
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
import static com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry.AnnotationResultTypes.LIGHTBULB_STORAGE_DESCRIPTION;

public class ParcelableAnnotationReader extends AnnotationReader {
    private final List<LightbulbParcelableDescription> parcelableDescriptions = new ArrayList<>();

    public ParcelableAnnotationReader(AnnotationResultsRegistry resultsRegistry, Messager messager, Elements elements, RoundEnvironment environment) {
        super(resultsRegistry, messager, elements, environment);
    }

    @Override
    protected void handleAnnotationsForClass(TypeElement target, List<AnnotatedElement> annotatedElements) {
        LightbulbParcelableDescription.Builder parcelableDescriptionBuilder = new LightbulbParcelableDescription.Builder(elements, target);

        annotatedElements.forEach(element -> {
            Annotation annotation = element.getAnnotation();

            if (annotation instanceof LightbulbParcelable lightbulbParcelable) {
                List<Field> fields = ElementUtils.getFieldElements((TypeElement) element.getElement())
                        .stream()
                        .map(Field::new)
                        .toList();

                parcelableDescriptionBuilder.withFields(fields);
            }
        });

        parcelableDescriptions.add(parcelableDescriptionBuilder.build());
    }

    @Override
    protected void onAnnotationsExtracted(AnnotationResultsRegistry resultRegistry) {
        resultRegistry.setResult(LIGHTBULB_PARCELABLE_DESCRIPTION, parcelableDescriptions);
    }

    @Override
    protected Map<Class<? extends Annotation>, ElementKind> getAnnotationTargets() {
        Map<Class<? extends Annotation>, ElementKind> targets = new HashMap<>();
        targets.put(LightbulbParcelable.class, ElementKind.CLASS);
        return targets;
    }
}
