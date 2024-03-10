package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.ClassMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.ClassDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;

import java.util.List;

public final class ApplicationMetadata extends ClassMetadata {

    public ApplicationMetadata(ClassDefinition activityClassDefinition, List<AnnotatedElement> annotatedElements) {
        super(activityClassDefinition);
    }
}