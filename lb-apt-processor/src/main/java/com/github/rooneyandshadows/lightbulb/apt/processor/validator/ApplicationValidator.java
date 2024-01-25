package com.github.rooneyandshadows.lightbulb.apt.processor.validator;

import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbApplication;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.ApplicationMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.validator.base.AnnotationResultValidator;

import javax.annotation.processing.Messager;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.commons.ClassNames.ANDROID_APPLICATION_CANONICAL_NAME;
import static java.lang.String.format;
import static javax.tools.Diagnostic.Kind.ERROR;

public class ApplicationValidator extends AnnotationResultValidator {

    public ApplicationValidator(Messager messager, AnnotationResultsRegistry annotationResultsRegistry) {
        super(messager, annotationResultsRegistry);
    }

    @Override
    protected boolean validateResult(Messager messager, AnnotationResultsRegistry annotationResultsRegistry) {
        List<ApplicationMetadata> applicationDescriptions = annotationResultsRegistry.getApplicationDescriptions();
        return validateApplications(applicationDescriptions, messager);
    }

    private boolean validateApplications(List<ApplicationMetadata> targets, Messager messager) {
        if (targets.isEmpty()) {
            return true;
        }
        boolean isValid = true;

        for (ApplicationMetadata target : targets) {
            ValidateResult validateResult = new ValidateResult(format("Problems found in class %s", target.getTypeInformation().getTypeMirror()));

            validateSuperClass(target, validateResult);

            if (!validateResult.isValid()) {
                messager.printMessage(ERROR, validateResult.getErrorText());
            }

            isValid &= validateResult.isValid();
        }

        return isValid;
    }

    private void validateSuperClass(ApplicationMetadata target, ValidateResult validateResult) {
        if (!target.getTypeInformation().is(ANDROID_APPLICATION_CANONICAL_NAME)) {
            String errorTemplate = "Classes annotated with @%s must be subclasses of %s.";
            String errorLineString = String.format(errorTemplate, LightbulbApplication.class.getSimpleName(), ANDROID_APPLICATION_CANONICAL_NAME);
            validateResult.addError(new ValidateResult.ErrorLine(errorLineString));
        }
    }
}
