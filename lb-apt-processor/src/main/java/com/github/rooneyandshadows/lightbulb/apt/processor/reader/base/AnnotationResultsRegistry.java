package com.github.rooneyandshadows.lightbulb.apt.processor.reader.base;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unchecked", "SpellCheckingInspection"})
public final class AnnotationResultsRegistry {
    private final Map<AnnotationResultTypes, Object> results = new HashMap<>();

    public void setResult(AnnotationResultTypes key, Object value) {
        results.put(key, value);
    }

    public <T> T getResult(AnnotationResultTypes key) {
        return (T) results.get(key);
    }

    public enum AnnotationResultTypes {
        LIGHTBULB_FRAGMENT_DESCRIPTION,
        LIGHTBULB_ACTIVITY_DESCRIPTION,
        LIGHTBULB_STORAGE_DESCRIPTION
    }
}
