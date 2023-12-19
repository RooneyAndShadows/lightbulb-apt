package com.github.rooneyandshadows.lightbulb.apt.processor.generation_steps.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry;

public interface GenerationStep {
    void process(AnnotationResultsRegistry resultsRegistry);
}