package com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps;

import com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps.base.GenerationStep;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNameUtils;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

public class GenerateCodeStep implements GenerationStep {
    private final Filer filer;
    private final Elements elements;
    private final PackageNames packageNames;
    private final ClassNameUtils classNames;

    public GenerateCodeStep(String rootPackage, Filer filer, Elements elements) {
        this.filer = filer;
        this.elements = elements;
        this.packageNames = new PackageNames(rootPackage);
        this.classNames = new ClassNameUtils(packageNames);
    }

    @Override
    public boolean process(AnnotationResultsRegistry resultsRegistry) {
        List<CodeGenerator> generators = new ArrayList<>();

        generators.add(new ApplicationGenerator(filer, elements, packageNames, classNames, resultsRegistry));
        generators.add(new FragmentGenerator(filer, elements, packageNames, classNames, resultsRegistry));
        generators.add(new FragmentFactoryGenerator(filer, elements, packageNames, classNames, resultsRegistry));
        generators.add(new FragmentResultsGenerator(filer, elements, packageNames, classNames, resultsRegistry));
        generators.add(new RoutingGenerator(filer, elements, packageNames, classNames, resultsRegistry));
        generators.add(new ActivityGenerator(filer, elements, packageNames, classNames, resultsRegistry));
        generators.add(new StorageGenerator(filer, elements, packageNames, classNames, resultsRegistry));
        generators.add(new ServiceGenerator(filer, elements, packageNames, classNames, resultsRegistry));
        generators.add(new ParcelableGenerator(filer, elements, packageNames, classNames, resultsRegistry));

        generators.forEach(CodeGenerator::generate);

        return true;
    }
}
