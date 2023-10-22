package com.github.rooneyandshadows.lightbulb.annotation_processors;

import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.ActivityGenerator;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.BindingRegistryGenerator;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.FragmentGenerator;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.RoutingGenerator;
import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.ActivityAnnotationReader;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.FragmentAnnotationReader;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationReader;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

public class CodeGenerationService {
    private final String rootPackage;
    private final Filer filer;
    private final Messager messager;
    private final Elements elements;
    private final RoundEnvironment roundEnvironment;
    private final List<AnnotationReader> readers = new ArrayList<>();
    private final List<CodeGenerator> generators = new ArrayList<>();
    private final AnnotationResultsRegistry resultsRegistry = new AnnotationResultsRegistry();

    public CodeGenerationService(String rootPackage, Filer filer, Messager messager, Elements elements, RoundEnvironment roundEnvironment) {
        this.rootPackage = rootPackage;
        this.filer = filer;
        this.messager = messager;
        this.elements = elements;
        this.roundEnvironment = roundEnvironment;
        init();
    }

    private void init() {
        readers.add(new ActivityAnnotationReader(resultsRegistry, messager, elements, roundEnvironment));
        readers.add(new FragmentAnnotationReader(resultsRegistry, messager, elements, roundEnvironment));
        generators.add(new FragmentGenerator(rootPackage, filer, resultsRegistry));
        generators.add(new RoutingGenerator(rootPackage, filer, resultsRegistry));
        generators.add(new ActivityGenerator(rootPackage, filer, resultsRegistry));
        generators.add(new BindingRegistryGenerator(rootPackage, filer, resultsRegistry));
    }

    public void process() {
        readers.forEach(AnnotationReader::readAnnotations);
        generators.forEach(CodeGenerator::generate);
    }
}
