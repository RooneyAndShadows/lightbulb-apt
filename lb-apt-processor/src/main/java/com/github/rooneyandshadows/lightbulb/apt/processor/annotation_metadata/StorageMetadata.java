package com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.ClassMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.FieldMetadata;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

public final class StorageMetadata extends ClassMetadata {
    private final String name;
    private final String[] subKeys;
    private final List<TargetField> targetFields;

    public StorageMetadata(TypeElement element, String name, String[] subKeys, List<TargetField> targetFields) {
        super(element);
        this.name = name;
        this.subKeys = subKeys;
        this.targetFields = targetFields;
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
        public TargetField(VariableElement element) {
            super(element);
        }
    }
}