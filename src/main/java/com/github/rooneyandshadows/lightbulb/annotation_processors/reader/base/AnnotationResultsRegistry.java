package com.github.rooneyandshadows.lightbulb.annotation_processors.reader.base;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class AnnotationResultsRegistry {
    private final Map<AnnotationResultTypes, Object> results = new HashMap<>();

    public void setResult(AnnotationResultTypes key, Object value) {
        results.put(key, value);
    }

    public <T> T getResult(AnnotationResultTypes key, Object value) {
        return (T) results.get(key);
    }

    public enum AnnotationResultTypes {
        FRAGMENT_BINDINGS,
        ACTIVITY_BINDINGS
    }
}
