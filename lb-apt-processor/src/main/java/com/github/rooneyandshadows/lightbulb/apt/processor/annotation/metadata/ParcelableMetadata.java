package com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.BaseMetadata;

import javax.lang.model.element.Element;
import java.util.List;

public final class ParcelableMetadata extends BaseMetadata {
    private List<TargetField> targetFields;

    public ParcelableMetadata(Element element, List<TargetField> targetFields) {
        super(element);
        this.targetFields = targetFields;
    }

    public List<TargetField> getTargetFields() {
        return targetFields;
    }

    public record TargetField(Element element) {
    }
}