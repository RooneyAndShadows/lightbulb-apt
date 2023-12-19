package com.github.rooneyandshadows.lightbulb.apt.processor.data;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.LightbulbActivityDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.LightbulbApplicationDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.LightbulbFragmentDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.LightbulbStorageDescription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry.AnnotationResultTypes.*;

@SuppressWarnings({"unchecked", "SpellCheckingInspection"})
public final class AnnotationResultsRegistry {
    private final Map<AnnotationResultTypes, Object> results = new HashMap<>();

    public void setResult(AnnotationResultTypes key, Object value) {
        results.put(key, value);
    }

    private <T> T getResult(AnnotationResultTypes key) {
        return (T) results.get(key);
    }

    public List<LightbulbApplicationDescription> getApplicationDescriptions() {
        return getResult(LIGHTBULB_APPLICATION_DESCRIPTION);
    }

    public List<LightbulbFragmentDescription> getFragmentDescriptions() {
        return getResult(LIGHTBULB_FRAGMENT_DESCRIPTION);
    }

    public List<LightbulbActivityDescription> getActivityDescriptions() {
        return getResult(LIGHTBULB_ACTIVITY_DESCRIPTION);
    }

    public List<LightbulbStorageDescription> getStorageDescriptions() {
        return getResult(LIGHTBULB_STORAGE_DESCRIPTION);
    }

    public boolean hasApplicationDescriptions() {
        return !getApplicationDescriptions().isEmpty();
    }

    public boolean hasActivityDescriptions() {
        return !getActivityDescriptions().isEmpty();
    }

    public boolean hasFragmentDescriptions() {
        return !getFragmentDescriptions().isEmpty();
    }

    public boolean hasStorageDescriptions() {
        return !getStorageDescriptions().isEmpty();
    }

    public boolean hasRoutingScreens() {
        List<LightbulbFragmentDescription> fragmentBindings = getFragmentDescriptions();
        return fragmentBindings.stream().anyMatch(LightbulbFragmentDescription::isScreen);
    }

    public enum AnnotationResultTypes {
        LIGHTBULB_FRAGMENT_DESCRIPTION,
        LIGHTBULB_ACTIVITY_DESCRIPTION,
        LIGHTBULB_APPLICATION_DESCRIPTION,
        LIGHTBULB_STORAGE_DESCRIPTION
    }
}
