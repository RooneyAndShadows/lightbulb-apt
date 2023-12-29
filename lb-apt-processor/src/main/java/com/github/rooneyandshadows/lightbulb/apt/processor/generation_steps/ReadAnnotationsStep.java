package com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps;

import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps.base.GenerationStep;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.*;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationReader;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

public class ReadAnnotationsStep implements GenerationStep {
    private final Messager messager;
    private final Elements elements;
    private final RoundEnvironment roundEnvironment;


    public ReadAnnotationsStep(Messager messager, Elements elements, RoundEnvironment roundEnvironment) {
        this.messager = messager;
        this.elements = elements;
        this.roundEnvironment = roundEnvironment;
    }

    @Override
    public void process(AnnotationResultsRegistry resultsRegistry) {
        List<AnnotationReader> readers = new ArrayList<>();
        readers.add(new ApplicationAnnotationReader(resultsRegistry, messager, elements, roundEnvironment));
        readers.add(new ActivityAnnotationReader(resultsRegistry, messager, elements, roundEnvironment));
        readers.add(new FragmentAnnotationReader(resultsRegistry, messager, elements, roundEnvironment));
        readers.add(new StorageAnnotationReader(resultsRegistry, messager, elements, roundEnvironment));
        readers.add(new ParcelableAnnotationReader(resultsRegistry, messager, elements, roundEnvironment));
        readers.forEach(AnnotationReader::readAnnotations);
    }
}
