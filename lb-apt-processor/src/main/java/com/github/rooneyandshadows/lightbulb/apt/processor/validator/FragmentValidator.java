package com.github.rooneyandshadows.lightbulb.apt.processor.validator;

import com.github.rooneyandshadows.lightbulb.apt.annotations.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.FragmentMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.validator.base.AnnotationResultValidator;

import javax.annotation.processing.Messager;
import java.util.*;

import static com.github.rooneyandshadows.lightbulb.apt.commons.ClassNames.*;
import static java.lang.String.format;
import static javax.tools.Diagnostic.Kind.ERROR;

public class FragmentValidator extends AnnotationResultValidator {

    public FragmentValidator(Messager messager, AnnotationResultsRegistry annotationResultsRegistry) {
        super(messager, annotationResultsRegistry);
    }

    @Override
    protected boolean validateResult(Messager messager, AnnotationResultsRegistry annotationResultsRegistry) {
        List<FragmentMetadata> fragmentDescriptions = annotationResultsRegistry.getFragmentDescriptions();
        return validateFragments(fragmentDescriptions, messager);
    }

    private boolean validateFragments(List<FragmentMetadata> targets, Messager messager) {
        if (targets.isEmpty()) {
            return true;
        }
        boolean isValid = true;

        for (FragmentMetadata fragmentMetadata : targets) {
            ErrorString errorMessage = new ErrorString("Problems found in class %s", fragmentMetadata.getTypeInformation().getTypeMirror());

            boolean isMetadataValid = validateSuperclass(fragmentMetadata, errorMessage);
            isMetadataValid &= validateBindViews(fragmentMetadata, errorMessage);
            isMetadataValid &= validateViewDataBinding(fragmentMetadata, errorMessage);
            isMetadataValid &= validateViewModel(fragmentMetadata, errorMessage);

            if (!isMetadataValid) {
                messager.printMessage(ERROR, errorMessage.getErrorString());
            }

            isValid &= isMetadataValid;
        }

        return isValid;
    }

    private boolean validateSuperclass(FragmentMetadata fragmentMetadata, ErrorString errorMessage) {
        if (!fragmentMetadata.getTypeInformation().is(ANDROID_FRAGMENT_CANONICAL_NAME)) {
            errorMessage.append("\n\tClass must be subclass of %s.", ANDROID_FRAGMENT_CANONICAL_NAME);
            return false;
        }
        return true;
    }

    private boolean validateViewDataBinding(FragmentMetadata fragmentMetadata, ErrorString errorMessage) {
        if (!fragmentMetadata.getViewBindings().get(0).getTypeInformation().is(ANDROID_VIEW_DATA_BINDING_CANONICAL_NAME)) {
            errorMessage.append("\n\tField \"%s\" is annotated with @%s and it's type must be subtype of %s.", fragmentMetadata.getViewBindings().get(0).getName(), FragmentViewBinding.class.getSimpleName(), ANDROID_VIEW_DATA_BINDING_CANONICAL_NAME);
            return false;
        }
        return true;
    }

    private boolean validateViewModel(FragmentMetadata fragmentMetadata, ErrorString errorMessage) {
        if (!fragmentMetadata.getViewModels().get(0).getTypeInformation().is(ANDROID_VIEW_MODEL_CANONICAL_NAME)) {
            errorMessage.append("\n\tField \"%s\" is annotated with @%s and it's type must be subtype of %s.", fragmentMetadata.getViewBindings().get(0).getName(), FragmentViewModel.class.getSimpleName(), ANDROID_VIEW_MODEL_CANONICAL_NAME);
            return false;
        }
        return true;
    }

    private boolean validateBindViews(FragmentMetadata fragmentMetadata, ErrorString errorMessage) {
        int errorsCount = 0;
        String requiredTypeName = ANDROID_VIEW_CANONICAL_NAME;
        String annotationName = BindView.class.getSimpleName();
        for (int i = 0; i < fragmentMetadata.getBindViews().size(); i++) {
            FragmentMetadata.BindView bindViewMetadata = fragmentMetadata.getBindViews().get(i);
            if (!bindViewMetadata.getTypeInformation().is(requiredTypeName)) {
                if (errorsCount == 0) {
                    errorsCount++;
                    errorMessage.append("\n\t@%s errors:", annotationName);
                }
                errorMessage.append("\n\t\tField \"%s\" is annotated with @%s and it's type must be subtype of %s.", bindViewMetadata.getName(), annotationName, requiredTypeName);
            }
        }
        return errorsCount == 0;
    }
}