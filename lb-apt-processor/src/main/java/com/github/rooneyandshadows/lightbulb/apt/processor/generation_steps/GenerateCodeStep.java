package com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps.base.GenerationStep;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.ActivityAnnotationReader;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.FragmentAnnotationReader;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.StorageAnnotationReader;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationReader;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

public class GenerateCodeStep implements GenerationStep {
    private final Filer filer;

    public GenerateCodeStep(Filer filer) {
        this.filer = filer;
    }

    @Override
    public void process(AnnotationResultsRegistry resultsRegistry) {
        List<CodeGenerator> generators = new ArrayList<>();
        generators.add(new ApplicationGenerator(filer, resultsRegistry));
        generators.add(new FragmentGenerator(filer, resultsRegistry));
        generators.add(new FragmentFactoryGenerator(filer, resultsRegistry));
        generators.add(new RoutingGenerator(filer, resultsRegistry));
        generators.add(new ActivityGenerator(filer, resultsRegistry));
        generators.add(new StorageGenerator(filer, resultsRegistry));
        generators.add(new ServiceGenerator(filer, resultsRegistry));
        generators.add(new ParcelableGenerator(filer, resultsRegistry));
        generators.forEach(CodeGenerator::generate);
    }
}
