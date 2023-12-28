package com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps.base.GenerationStep;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

public class GenerateCodeStep implements GenerationStep {
    private final Filer filer;
    private final Elements elements;

    public GenerateCodeStep(Filer filer, Elements elements) {
        this.filer = filer;
        this.elements = elements;
    }

    @Override
    public void process(AnnotationResultsRegistry resultsRegistry) {
        List<CodeGenerator> generators = new ArrayList<>();
        generators.add(new ApplicationGenerator(filer, elements, resultsRegistry));
        generators.add(new FragmentGenerator(filer, elements, resultsRegistry));
        generators.add(new FragmentFactoryGenerator(filer, elements, resultsRegistry));
        generators.add(new RoutingGenerator(filer, elements, resultsRegistry));
        generators.add(new ActivityGenerator(filer, elements, resultsRegistry));
        generators.add(new StorageGenerator(filer, elements, resultsRegistry));
        generators.add(new ServiceGenerator(filer, elements, resultsRegistry));
        generators.add(new ParcelableGenerator(filer, elements, resultsRegistry));
        generators.forEach(CodeGenerator::generate);
    }
}
