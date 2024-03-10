package com.github.rooneyandshadows.lightbulb.apt.processor.validator;

import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbParcelable;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.ParcelableMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.ParcelableMetadata.IgnoredField;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.FieldMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.validator.base.AnnotationResultValidator;

import javax.annotation.processing.Messager;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.commons.ClassDefinitions.PARCELABLE;
import static java.lang.String.format;
import static javax.tools.Diagnostic.Kind.ERROR;

public class ParcelableValidator extends AnnotationResultValidator {
    private final List<ParcelableMetadata> parcelableDescriptions;

    public ParcelableValidator(Messager messager, AnnotationResultsRegistry annotationResultsRegistry) {
        super(messager, annotationResultsRegistry);
        parcelableDescriptions = annotationResultsRegistry.getParcelableDescriptions();
    }

    @Override
    protected boolean validateResult(Messager messager, AnnotationResultsRegistry annotationResultsRegistry) {
        if (parcelableDescriptions.isEmpty()) {
            return true;
        }
        return validateParcelables(messager);
    }

    private boolean validateParcelables(Messager messager) {
        boolean isValid = true;

        for (ParcelableMetadata target : parcelableDescriptions) {
            ErrorString errorMessage = new ErrorString("Problems found in class %s", target.getTypeDefinition().getTypeMirror());

            boolean isMetadataValid = validateSuperClass(target, errorMessage);
             isMetadataValid &= validateParcelIgnoredFields(target, errorMessage);

            if (!isMetadataValid) {
                messager.printMessage(ERROR, errorMessage.getErrorString());
            }

            isValid &= isMetadataValid;
        }

        return isValid;
    }

    private boolean validateSuperClass(ParcelableMetadata target, ErrorString errorString) {
        if (!target.getTypeDefinition().is(PARCELABLE)) {
            errorString.append("\n\tClasses annotated with @%s must be subclasses of %s.", LightbulbParcelable.class.getSimpleName(), PARCELABLE);
            return false;
        }
        return true;
    }

    private boolean validateParcelIgnoredFields(ParcelableMetadata target, ErrorString errorMessage) {
        List<IgnoredField> ignoredFinalFields = target.getIgnoredFields().stream().filter(FieldMetadata::isFinal).toList();

        if (ignoredFinalFields.isEmpty()) {
            return true;
        }
        errorMessage.append("\n\tIgnored fields for classes annotated with @%s cannot be final. Problems found:", LightbulbParcelable.class.getSimpleName());

        for (IgnoredField field : ignoredFinalFields) {
            String errorField = field.getName();
            String accessModifier = field.getAccessModifier().toString();
            String type = field.getTypeDefinition().getTypeMirror().toString();

            errorMessage.append("\n\t\t %s final %s %s;", accessModifier, type, errorField);
        }

        return false;
    }
}
