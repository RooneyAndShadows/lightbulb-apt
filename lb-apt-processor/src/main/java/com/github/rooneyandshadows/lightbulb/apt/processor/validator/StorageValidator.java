package com.github.rooneyandshadows.lightbulb.apt.processor.validator;

import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.validator.base.AnnotationResultValidator;

import javax.annotation.processing.Messager;
import java.util.Locale;

import static javax.tools.Diagnostic.Kind.ERROR;

public class StorageValidator extends AnnotationResultValidator {

    public StorageValidator(Messager messager, AnnotationResultsRegistry annotationResultsRegistry) {
        super(messager, annotationResultsRegistry);
    }

    @Override
    protected boolean validateResult(Messager messager, AnnotationResultsRegistry annotationResultsRegistry) {
        if (annotationResultsRegistry.hasStorageDescriptions() && !annotationResultsRegistry.hasApplicationDescriptions()) {
            messager.printMessage(ERROR, "In order to use @LightbulbStorage you must have application class annotated with @LightbulbApplication and declared in the manifest file.");
            return false;
        }
        return true;
    }
}
