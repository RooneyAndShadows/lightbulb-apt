package com.github.rooneyandshadows.lightbulb.apt.processor;

import com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps.GenerateCodeStep;
import com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps.ReadAnnotationsStep;
import com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps.base.GenerationStep;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

public class CodeGenerationService {
    private final List<GenerationStep> steps = new ArrayList<>();
    private final AnnotationResultsRegistry resultsRegistry = new AnnotationResultsRegistry();

    public CodeGenerationService(String rootPackage, Filer filer, Messager messager, Elements elements, RoundEnvironment roundEnvironment) {
        steps.add(new ReadAnnotationsStep(messager, elements, roundEnvironment));
        steps.add(new GenerateCodeStep(rootPackage, filer, elements));
    }

    public void process() {
        steps.forEach(generationStep -> generationStep.process(resultsRegistry));
    }
}