package com.github.rooneyandshadows.lightbulb.annotation_processors.generator;

import com.github.rooneyandshadows.lightbulb.annotation_processors.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry;

import javax.annotation.processing.Filer;

public class ActivityGenerator extends CodeGenerator {

    public ActivityGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, annotationResultsRegistry);
    }

    @Override
    public void generate() {

    }
}
