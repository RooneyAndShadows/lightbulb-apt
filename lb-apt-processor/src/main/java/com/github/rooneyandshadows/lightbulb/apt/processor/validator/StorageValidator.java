package com.github.rooneyandshadows.lightbulb.apt.processor.validator;

import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbApplication;
import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbStorage;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.validator.base.AnnotationResultValidator;

import javax.annotation.processing.Messager;

import static javax.tools.Diagnostic.Kind.ERROR;

public class StorageValidator extends AnnotationResultValidator {

    public StorageValidator(Messager messager, AnnotationResultsRegistry annotationResultsRegistry) {
        super(messager, annotationResultsRegistry);
    }

    @Override
    protected boolean validateResult(Messager messager, AnnotationResultsRegistry annotationResultsRegistry) {
        if (annotationResultsRegistry.hasStorageDescriptions() && !annotationResultsRegistry.hasApplicationDescriptions()) {
            ErrorString errorMessage = new ErrorString("In order to use @%s you must have application class annotated with @%s and declared in the manifest file.", LightbulbStorage.class.getSimpleName(), LightbulbApplication.class.getSimpleName());
            messager.printMessage(ERROR, errorMessage.getErrorString());
            return false;
        }
        return true;
    }
}
