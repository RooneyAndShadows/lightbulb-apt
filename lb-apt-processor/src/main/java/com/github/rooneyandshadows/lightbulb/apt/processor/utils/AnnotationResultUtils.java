package com.github.rooneyandshadows.lightbulb.apt.processor.utils;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.LightbulbActivityDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.LightbulbFragmentDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.LightbulbStorageDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;

import java.util.*;

import static com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry.AnnotationResultTypes.*;

public class AnnotationResultUtils {

    public static List<LightbulbFragmentDescription> getFragmentDescriptions(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.getResult(LIGHTBULB_FRAGMENT_DESCRIPTION);
    }

    public static List<LightbulbActivityDescription> getActivityDescriptions(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.getResult(LIGHTBULB_ACTIVITY_DESCRIPTION);
    }

    public static List<LightbulbStorageDescription> getStorageDescriptions(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.getResult(LIGHTBULB_STORAGE_DESCRIPTION);
    }

    public static boolean hasStorageElements(AnnotationResultsRegistry annotationResultsRegistry) {
        return !getStorageDescriptions(annotationResultsRegistry).isEmpty();
    }

    public static boolean hasRoutingScreens(AnnotationResultsRegistry annotationResultsRegistry) {
        List<LightbulbFragmentDescription> fragmentBindings = getFragmentDescriptions(annotationResultsRegistry);
        return AnnotationResultUtils.hasScreens(fragmentBindings);
    }

    private static boolean hasScreens(List<LightbulbFragmentDescription> fragmentBindings) {
        return fragmentBindings.stream().anyMatch(LightbulbFragmentDescription::isScreen);
    }
}
