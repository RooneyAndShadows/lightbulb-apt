package com.github.rooneyandshadows.lightbulb.apt.processor.validator;

import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbApplication;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.ApplicationMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.validator.base.AnnotationResultValidator;

import javax.annotation.processing.Messager;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.commons.ClassNames.ANDROID_APPLICATION_CANONICAL_NAME;
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
            ErrorString errorMessage = new ErrorString("Problems found in class %s", target.getTypeInformation().getTypeMirror());

            boolean isMetadataValid = validateSuperClass(target, errorMessage);

            if (!isMetadataValid) {
                messager.printMessage(ERROR, errorMessage.getErrorString());
            }

            isValid &= isMetadataValid;
        }

        return isValid;
    }

    private boolean validateSuperClass(ApplicationMetadata target, ErrorString errorString) {
        if (!target.getTypeInformation().is(ANDROID_APPLICATION_CANONICAL_NAME)) {
            errorString.append("Classes annotated with @%s must be subclasses of %s.", LightbulbApplication.class.getSimpleName(), ANDROID_APPLICATION_CANONICAL_NAME);
            return false;
        }
        return true;
    }
}
