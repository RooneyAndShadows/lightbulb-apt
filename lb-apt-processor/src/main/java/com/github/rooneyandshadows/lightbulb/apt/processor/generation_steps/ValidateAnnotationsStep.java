package com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps;

import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps.base.GenerationStep;
import com.github.rooneyandshadows.lightbulb.apt.processor.validator.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.validator.base.AnnotationResultValidator;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
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
        validators.add(new ActivityValidator(messager, resultsRegistry));
        validators.add(new ParcelableValidator(messager, resultsRegistry));

        boolean result = true;

        for (AnnotationResultValidator validator : validators) {
            result &= validator.validate();
        }

        return result;
    }
}