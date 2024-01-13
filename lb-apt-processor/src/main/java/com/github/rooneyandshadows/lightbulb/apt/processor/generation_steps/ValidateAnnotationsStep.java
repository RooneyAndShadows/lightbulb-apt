package com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps;

import com.github.rooneyandshadows.lightbulb.apt.annotations.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.BaseMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.ClassMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps.base.GenerationStep;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Locale;

import static com.github.rooneyandshadows.lightbulb.apt.commons.ClassNames.*;
import static javax.tools.Diagnostic.Kind.ERROR;

public class ValidateAnnotationsStep implements GenerationStep {
    private final Messager messager;
    private final Elements elements;
    private final RoundEnvironment roundEnvironment;

    public ValidateAnnotationsStep(Messager messager, Elements elements, RoundEnvironment roundEnvironment) {
        this.messager = messager;
        this.elements = elements;
        this.roundEnvironment = roundEnvironment;
    }

    @Override
    public boolean process(AnnotationResultsRegistry resultsRegistry) {
        boolean isValid = validateStorage(resultsRegistry);

        isValid &= requireSuperclass(LightbulbApplication.class, ANDROID_APPLICATION_CANONICAL_NAME, resultsRegistry.getApplicationDescriptions());
        isValid &= requireSuperclass(LightbulbFragment.class, ANDROID_FRAGMENT_CANONICAL_NAME, resultsRegistry.getFragmentDescriptions());
        isValid &= requireSuperclass(FragmentScreen.class, ANDROID_FRAGMENT_CANONICAL_NAME, resultsRegistry.getFragmentDescriptions());
        isValid &= requireSuperclass(LightbulbActivity.class, ANDROID_ACTIVITY_CANONICAL_NAME, resultsRegistry.getActivityDescriptions());
        isValid &= requireSuperclass(LightbulbParcelable.class, ANDROID_PARCELABLE_CANONICAL_NAME, resultsRegistry.getParcelableDescriptions());

        return isValid;
    }

    private boolean validateStorage(AnnotationResultsRegistry resultsRegistry) {
        if (resultsRegistry.hasStorageDescriptions() && !resultsRegistry.hasApplicationDescriptions()) {
            messager.printMessage(ERROR, "In order to use @LightbulbStorage you must have application class annotated with @LightbulbApplication and declared in the manifest file.");
            return false;
        }
        return true;
    }

    private boolean requireSuperclass(Class<? extends Annotation> targetAnnotation, String requiredClassCannonicalName, List<? extends ClassMetadata> targets) {
        if (!targets.isEmpty()) {
            String errorMessage = String.format("Classes annotated with @%s must be subclasses of %s. Problems found:", targetAnnotation.getSimpleName(), requiredClassCannonicalName);
            int count = 0;
            for (BaseMetadata<TypeElement> target : targets) {
                boolean extendsFragment = target.getTypeInformation().is(requiredClassCannonicalName);
                if (!extendsFragment) {
                    count++;
                    String errorType = target.getTypeInformation().getTypeMirror().toString();
                    String errorLine = String.format(Locale.getDefault(), "\n%d: %s", count, errorType);
                    errorMessage = errorMessage.concat(errorLine);
                }
            }
            if (count > 0) {
                messager.printMessage(ERROR, errorMessage);
                return false;
            }
        }
        return true;
    }
}
