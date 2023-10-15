package com.github.rooneyandshadows.lightbulb.annotation_processors.generator.base;

import com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base.AnnotationResultsRegistry;

import javax.annotation.processing.Filer;

public abstract class CodeGenerator {
    protected final String rootPackage;
    protected final Filer filer;
    protected final AnnotationResultsRegistry annotationResultsRegistry;

    public CodeGenerator(String rootPackage, Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        this.rootPackage = rootPackage;
        this.filer = filer;
        this.annotationResultsRegistry = annotationResultsRegistry;
    }

    public abstract void generate();
}
