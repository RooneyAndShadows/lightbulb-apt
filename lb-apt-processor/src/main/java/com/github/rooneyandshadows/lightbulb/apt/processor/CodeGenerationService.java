package com.github.rooneyandshadows.lightbulb.apt.processor;

import com.github.rooneyandshadows.lightbulb.apt.processor.generator.FragmentFactoryGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.RoutingGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.ActivityAnnotationReader;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.FragmentAnnotationReader;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationReader;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.ActivityGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.FragmentGenerator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

public class CodeGenerationService {
    private final Filer filer;
    private final Messager messager;
    private final Elements elements;
    private final RoundEnvironment roundEnvironment;
    private final List<AnnotationReader> readers = new ArrayList<>();
    private final List<CodeGenerator> generators = new ArrayList<>();
    private final AnnotationResultsRegistry resultsRegistry = new AnnotationResultsRegistry();

    public CodeGenerationService(Filer filer, Messager messager, Elements elements, RoundEnvironment roundEnvironment) {
        this.filer = filer;
        this.messager = messager;
        this.elements = elements;
        this.roundEnvironment = roundEnvironment;
        init();
    }

    private void init() {
        readers.add(new ActivityAnnotationReader(resultsRegistry, messager, elements, roundEnvironment));
        readers.add(new FragmentAnnotationReader(resultsRegistry, messager, elements, roundEnvironment));
        generators.add(new FragmentGenerator(filer, resultsRegistry));
        generators.add(new FragmentFactoryGenerator(filer, resultsRegistry));
        generators.add(new RoutingGenerator(filer, resultsRegistry));
        generators.add(new ActivityGenerator(filer, resultsRegistry));
        //  generators.add(new BindingRegistryGenerator(rootPackage, filer, resultsRegistry));
    }

    public void process() {
        readers.forEach(AnnotationReader::readAnnotations);
        generators.forEach(CodeGenerator::generate);
    }
}
