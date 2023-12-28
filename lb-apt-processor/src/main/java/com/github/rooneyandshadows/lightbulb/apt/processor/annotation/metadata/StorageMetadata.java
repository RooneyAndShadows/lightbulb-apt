package com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.BaseMetadata;

import javax.lang.model.element.Element;
import java.util.List;

public final class StorageMetadata extends BaseMetadata {
    private final String name;
    private final String[] subKeys;
    private final List<TargetField> targetFields;

    public StorageMetadata(Element element, String name, String[] subKeys, List<TargetField> targetFields) {
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

    public record TargetField(Element element) {
    }
}