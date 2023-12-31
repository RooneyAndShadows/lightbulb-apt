package com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.ClassMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.FieldMetadata;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

public final class ParcelableMetadata extends ClassMetadata {
    private final List<TargetField> targetFields;

    public ParcelableMetadata(TypeElement element, List<TargetField> targetFields) {
        super(element);
        this.targetFields = targetFields;
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