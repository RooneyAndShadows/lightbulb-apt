package com.github.rooneyandshadows.lightbulb.apt.processor.validator;

import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbActivity;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.ActivityMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.validator.base.AnnotationResultValidator;

import javax.annotation.processing.Messager;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.commons.ClassDefinitions.ACTIVITY;
import static javax.tools.Diagnostic.Kind.ERROR;

public class ActivityValidator extends AnnotationResultValidator {

    public ActivityValidator(Messager messager, AnnotationResultsRegistry annotationResultsRegistry) {
        super(messager, annotationResultsRegistry);
    }

    @Override
    protected boolean validateResult(Messager messager, AnnotationResultsRegistry annotationResultsRegistry) {
        List<ActivityMetadata> activityDescriptions = annotationResultsRegistry.getActivityDescriptions();
        return validateActivities(activityDescriptions, messager);
    }

    private boolean validateActivities(List<ActivityMetadata> targets, Messager messager) {
        if (targets.isEmpty()) {
            return true;
        }

        boolean isValid = true;

        for (ActivityMetadata target : targets) {
            ErrorString errorMessage = new ErrorString("Problems found in class %s", target.getTypeInformation().getTypeMirror());

            boolean isMetadataValid = validateSuperClass(target, errorMessage);

            if (!isMetadataValid) {
                messager.printMessage(ERROR, errorMessage.getErrorString());
            }

            isValid &= isMetadataValid;
        }

        return isValid;
    }

    private boolean validateSuperClass(ActivityMetadata target, ErrorString errorString) {
        if (!target.getTypeInformation().is(ACTIVITY)) {
            errorString.append("Classes annotated with @%s must be subclasses of %s.", LightbulbActivity.class.getSimpleName(), ACTIVITY);
            return false;
        }
        return true;
    }
}
