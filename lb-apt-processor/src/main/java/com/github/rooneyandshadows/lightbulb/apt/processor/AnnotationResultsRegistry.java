package com.github.rooneyandshadows.lightbulb.apt.processor;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry.AnnotationResultTypes.*;

@SuppressWarnings({"unchecked", "SpellCheckingInspection"})
public final class AnnotationResultsRegistry {
    private final Map<AnnotationResultTypes, Object> results = new HashMap<>();

    public void setResult(AnnotationResultTypes key, Object value) {
        results.put(key, value);
    }

    private <T> T getResult(AnnotationResultTypes key) {
        return (T) results.get(key);
    }

    public List<ApplicationMetadata> getApplicationDescriptions() {
        return getResult(LIGHTBULB_APPLICATION_DESCRIPTION);
    }

    public List<FragmentMetadata> getFragmentDescriptions() {
        return getResult(LIGHTBULB_FRAGMENT_DESCRIPTION);
    }

    public List<ActivityMetadata> getActivityDescriptions() {
        return getResult(LIGHTBULB_ACTIVITY_DESCRIPTION);
    }

    public List<StorageMetadata> getStorageDescriptions() {
        return getResult(LIGHTBULB_STORAGE_DESCRIPTION);
    }

    public List<ParcelableMetadata> getParcelableDescriptions() {
        return getResult(LIGHTBULB_PARCELABLE_DESCRIPTION);
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

    public boolean hasParcelableDescriptions() {
        return !getParcelableDescriptions().isEmpty();
    }

    public boolean hasRoutingScreens() {
        List<FragmentMetadata> fragmentBindings = getFragmentDescriptions();
        return fragmentBindings.stream().anyMatch(FragmentMetadata::isScreen);
    }

    public enum AnnotationResultTypes {
        LIGHTBULB_FRAGMENT_DESCRIPTION,
        LIGHTBULB_ACTIVITY_DESCRIPTION,
        LIGHTBULB_APPLICATION_DESCRIPTION,
        LIGHTBULB_STORAGE_DESCRIPTION,
        LIGHTBULB_PARCELABLE_DESCRIPTION
    }
}
