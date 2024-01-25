package com.github.rooneyandshadows.lightbulb.apt.processor.validator;

import com.github.rooneyandshadows.lightbulb.apt.annotations.BindView;
import com.github.rooneyandshadows.lightbulb.apt.annotations.FragmentParameter;
import com.github.rooneyandshadows.lightbulb.apt.annotations.FragmentScreen;
import com.github.rooneyandshadows.lightbulb.apt.annotations.FragmentViewBinding;
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
            ValidateResult validateResult = new ValidateResult(format("Problems found in class %s", fragmentMetadata.getTypeInformation().getTypeMirror()));

            validateSuperclass(fragmentMetadata, validateResult);
            validateFragmentScreen(fragmentMetadata, validateResult);
            validateViewDataBinding(fragmentMetadata, validateResult);
            validateBindViews(fragmentMetadata, validateResult);

            if (!validateResult.isValid()) {
                messager.printMessage(ERROR, validateResult.getErrorText());
            }

            isValid &= validateResult.isValid();
        }

        return isValid;
    }

    private void validateSuperclass(FragmentMetadata fragmentMetadata, ValidateResult validationResult) {
        if (!fragmentMetadata.getTypeInformation().is(ANDROID_FRAGMENT_CANONICAL_NAME)) {
            String errorLine = format("Class must be subclass of %s.", ANDROID_FRAGMENT_CANONICAL_NAME);
            validationResult.addError(new ValidateResult.ErrorLine(errorLine));
        }
    }

    private void validateFragmentScreen(FragmentMetadata fragmentMetadata, ValidateResult validationResult) {
        if (fragmentMetadata.hasParameters() && !fragmentMetadata.isScreen()) {
            String errorTemplate = "Class has fields annotaded with %s but it's not annotated with %s.";
            String errorLine = format(errorTemplate, FragmentParameter.class.getSimpleName(), FragmentScreen.class.getSimpleName());
            validationResult.addError(new ValidateResult.ErrorLine(errorLine));
        }
    }

    private void validateViewDataBinding(FragmentMetadata fragmentMetadata, ValidateResult validationResult) {
        FragmentMetadata.ViewBinding fieldMetadata = fragmentMetadata.getViewBindings().get(0);

        if (!fieldMetadata.getTypeInformation().is(ANDROID_VIEW_DATA_BINDING_CANONICAL_NAME)) {
            String errorLineTemplate = "Field \"%s\" is annotated with @%s and it's type must be subtype of %s.";
            String errorFieldName = fieldMetadata.getName();
            String annotationName = FragmentViewBinding.class.getSimpleName();
            String errorLine = format(errorLineTemplate, errorFieldName, annotationName, ANDROID_VIEW_DATA_BINDING_CANONICAL_NAME);
            validationResult.addError(new ValidateResult.ErrorLine(errorLine));
        }
    }


    private void validateBindViews(FragmentMetadata fragmentMetadata, ValidateResult validationResult) {
        for (int i = 0; i < fragmentMetadata.getBindViews().size(); i++) {
            String requiredTypeName = ANDROID_VIEW_CANONICAL_NAME;
            String annotationName = BindView.class.getSimpleName();
            FragmentMetadata.BindView bindViewMetadata = fragmentMetadata.getBindViews().get(i);
            String errorLineString = format("@%s errors:", annotationName);
            ValidateResult.ErrorLine errorLine = new ValidateResult.ErrorLine(errorLineString);
            if (!bindViewMetadata.getTypeInformation().is(requiredTypeName)) {
                String errorLineTemplate = "Field \"%s\" is annotated with @%s and it's type must be subtype of %s.";
                String errorFieldName = bindViewMetadata.getName();
                String subErrorString = format(errorLineTemplate, errorFieldName, annotationName, requiredTypeName);
                ValidateResult.ErrorLine subErrorLine = new ValidateResult.ErrorLine(subErrorString);
                errorLine.addSubError(subErrorLine);

                if (i == 0) {
                    validationResult.addError(errorLine);
                }
            }
        }
    }
}
