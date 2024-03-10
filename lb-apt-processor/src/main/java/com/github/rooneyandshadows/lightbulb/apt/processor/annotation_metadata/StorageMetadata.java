package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata;

import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbStorage;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.ClassMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.FieldMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.ClassDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.FieldDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotatedElement;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public final class StorageMetadata extends ClassMetadata {
    private String name;
    private String[] subKeys;
    private final List<TargetField> targetFields = new ArrayList<>();

    public StorageMetadata(ClassDefinition storageClassDefinition, List<AnnotatedElement> annotatedElements) {
        super(storageClassDefinition);
        extractValues(annotatedElements);
    }

    private void extractValues(List<AnnotatedElement> annotatedElements) {
        List<TargetField> targets = classDefinition.getFields().stream().map(TargetField::new).toList();
        targetFields.addAll(targets);

        for (AnnotatedElement element : annotatedElements) {
            Annotation annotation = element.getAnnotation();
            if (annotation instanceof LightbulbStorage lightbulbStorage) {
                name = lightbulbStorage.name();
                subKeys = lightbulbStorage.subKeys();
            }
        }
    }

    public String getName() {
        return name;
    }

    public String[] getSubKeys() {
        return subKeys;
    }

    public List<TargetField> getTargetFields() {
        return targetFields;
    }

    public static final class TargetField extends FieldMetadata {
        public TargetField(FieldDefinition fieldDefinition) {
            super(fieldDefinition);
        }
    }
}