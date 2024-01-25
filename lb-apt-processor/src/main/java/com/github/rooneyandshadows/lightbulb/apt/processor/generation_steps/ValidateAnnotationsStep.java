package com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps;

import com.github.rooneyandshadows.lightbulb.apt.annotations.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.FragmentMetadata.ViewBinding;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.ParcelableMetadata.IgnoredField;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.BaseMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.FieldMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps.base.GenerationStep;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.validator.ApplicationValidator;
import com.github.rooneyandshadows.lightbulb.apt.processor.validator.FragmentValidator;
import com.github.rooneyandshadows.lightbulb.apt.processor.validator.StorageValidator;
import com.github.rooneyandshadows.lightbulb.apt.processor.validator.base.AnnotationResultValidator;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.github.rooneyandshadows.lightbulb.apt.commons.ClassNames.*;
import static java.lang.String.*;
import static javax.tools.Diagnostic.Kind.ERROR;

@SuppressWarnings("FieldCanBeLocal")
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
        List<AnnotationResultValidator> validators = new ArrayList<>();
        validators.add(new FragmentValidator(messager, resultsRegistry));
        validators.add(new StorageValidator(messager, resultsRegistry));
        validators.add(new ApplicationValidator(messager, resultsRegistry));

        boolean result = true;

        for (AnnotationResultValidator validator : validators) {
            result &= validator.validate();
        }

        return result;
    }

    private boolean validateActivity(List<ActivityMetadata> targets) {
        if (targets.isEmpty()) {
            return true;
        }

        return requireSuperclassForClassMetadata(ANDROID_ACTIVITY_CANONICAL_NAME, targets, LightbulbActivity.class);
    }

    private boolean validateFragments(List<FragmentMetadata> targets) {
        if (targets.isEmpty()) {
            return true;
        }
        Locale locale = Locale.getDefault();

        for (FragmentMetadata fragmentMetadata : targets) {
            String errorString = format("Problems found in class %s", fragmentMetadata.getTypeInformation().getTypeMirror());
            int errorsCount = 0;

            //Superclass validation
            if (!fragmentMetadata.getTypeInformation().is(ANDROID_FRAGMENT_CANONICAL_NAME)) {
                errorsCount++;
                String errorLine = format(Locale.getDefault(), "\n%d. Class must be subclass of %s.", errorsCount, ANDROID_FRAGMENT_CANONICAL_NAME);
                errorString = errorString.concat(errorLine);
            }

            //@FragmentScreen validation
            if (fragmentMetadata.hasParameters() && !fragmentMetadata.isScreen()) {
                errorsCount++;
                String errorTemplate = "\n%d. Class has fields annotaded with %s but it's not annotated with %s.";
                String errorLine = format(locale, errorTemplate, errorsCount, FragmentParameter.class.getSimpleName(), FragmentScreen.class.getSimpleName());
                errorString = errorString.concat(errorLine);
            }

            //@ViewBinding validation
            ViewBinding fieldMetadata = fragmentMetadata.getViewBindings().get(0);

            if (!fieldMetadata.getTypeInformation().is(ANDROID_VIEW_DATA_BINDING_CANONICAL_NAME)) {
                errorsCount++;

                String errorLineTemplate = "\n%d. Field \"%s\" is annotated with @%s and it's type must be subtype of %s.";
                String errorFieldName = fieldMetadata.getName();
                String annotationName = FragmentViewBinding.class.getSimpleName();
                String errorLine = format(locale, errorLineTemplate, errorsCount, errorFieldName, annotationName, ANDROID_VIEW_DATA_BINDING_CANONICAL_NAME);

                errorString = errorString.concat(errorLine);
            }


            //@BindView validation
            int bindViewErrorsCount = 0;

            for (int i = 0; i < fragmentMetadata.getBindViews().size(); i++) {
                String requiredTypeName = ANDROID_VIEW_CANONICAL_NAME;
                String annotationName = BindView.class.getSimpleName();
                FragmentMetadata.BindView bindViewMetadata = fragmentMetadata.getBindViews().get(i);

                if (!bindViewMetadata.getTypeInformation().is(requiredTypeName)) {
                    if (i == 0) {
                        errorsCount++;

                        String errorLineTemplate = "\n%d. @%s errors:";
                        String errorLine = format(locale, errorLineTemplate, annotationName);

                        errorString = errorString.concat(errorLine);
                    }

                    bindViewErrorsCount++;

                    String errorLineTemplate = "\n%d.%d. Field \"%s\" is annotated with @%s and it's type must be subtype of %s.";
                    String errorFieldName = bindViewMetadata.getName();
                    String errorLine = format(Locale.getDefault(), errorLineTemplate, errorsCount, bindViewErrorsCount, errorFieldName, annotationName, requiredTypeName);

                    errorString = errorString.concat(errorLine);
                }
            }

            if (errorsCount > 0) {
                messager.printMessage(ERROR, errorString);
                return false;
            }
        }

        return isValid;
    }

    private boolean validateParcelable(List<ParcelableMetadata> targets) {
        if (targets.isEmpty()) {
            return true;
        }

        boolean result = requireSuperclassForClassMetadata(ANDROID_PARCELABLE_CANONICAL_NAME, targets, LightbulbParcelable.class);
        result &= requireParcelIgnoredFieldNotFinal(targets);

        return result;
    }

    private boolean requireParcelIgnoredFieldNotFinal(List<ParcelableMetadata> targets) {
        if (!targets.isEmpty()) {
            String errorMessage = format("Ignored fields for classes annotated with @%s cannot be final. Problems found:", LightbulbParcelable.class.getSimpleName());
            int classIndex = 0;
            for (ParcelableMetadata target : targets) {
                List<IgnoredField> fields = target.getIgnoredFields().stream().filter(FieldMetadata::isFinal).toList();
                if (!fields.isEmpty()) {
                    classIndex++;
                    String errorType = target.getTypeInformation().getTypeMirror().toString();
                    String errorTypeLine = format(Locale.getDefault(), "\n   %d: %s", classIndex, errorType);
                    errorMessage = errorMessage.concat(errorTypeLine);

                    int fieldIndex = 0;

                    for (IgnoredField field : fields) {
                        fieldIndex++;
                        String errorField = field.getName();
                        String accessModifier = field.getAccessModifier().toString();
                        String type = field.getTypeInformation().getTypeMirror().toString();
                        String errorFieldLine = format(Locale.getDefault(), "\n      %d.%d: %s final %s %s;", classIndex, fieldIndex, accessModifier, type, errorField);
                        errorMessage = errorMessage.concat(errorFieldLine);
                    }
                }
            }
            if (classIndex > 0) {
                messager.printMessage(ERROR, errorMessage);
                return false;
            }
        }
        return true;
    }

    private <T extends BaseMetadata<? extends Element>> boolean requireSuperclassForMetadata(String requiredClassCannonicalName, T target) {
        boolean result = true;

        boolean extendsRequiredClass = target.getTypeInformation().is(requiredClassCannonicalName);
        String errorTemplate = "Classes annotated with @%s must be subclasses of %s. Problems found:";


        String annotationName = targetAnnotation.getSimpleName();
        String errorMessage = format(errorTemplate, annotationName, requiredClassCannonicalName);
        int errorsFound = 0;

        for (T target : targets) {
            boolean hasAnnotation = target.getElement().getAnnotation(targetAnnotation) != null;
            boolean extendsRequiredClass = target.getTypeInformation().is(requiredClassCannonicalName);

            if (validationAction != null) {
                result &= validationAction.apply(target);
            }

            if (!hasAnnotation || extendsRequiredClass) {
                continue;
            }

            errorsFound++;
            String errorType = target.getTypeInformation().getTypeMirror().toString();
            String errorLine = format(Locale.getDefault(), "\n%d: %s", errorsFound, errorType);
            errorMessage = errorMessage.concat(errorLine);
        }

        if (errorsFound > 0) {
            messager.printMessage(ERROR, errorMessage);
            result = false;
        }


        return result;
    }
}