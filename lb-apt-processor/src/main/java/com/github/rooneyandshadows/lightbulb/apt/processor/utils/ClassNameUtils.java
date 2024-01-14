package com.github.rooneyandshadows.lightbulb.apt.processor.utils;

import com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.TypeInformation;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.BaseMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.ClassMetadata;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.TypeElement;
import java.time.OffsetDateTime;
import java.util.*;

import static com.github.rooneyandshadows.lightbulb.apt.commons.ClassNames.*;
import static com.github.rooneyandshadows.lightbulb.apt.commons.GeneratedClassNames.*;
import static com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames.ANDROID_BUILD_VERSION_CODES;


public class ClassNameUtils {
    /**
     * Lightbulb
     */
    public static final ClassName BASE_ROUTER = from(LIGHTBULB_BASE_ROUTER_CANONICAL_NAME);
    public static final ClassName BASE_STORAGE = from(LIGHTBULB_BASE_STORAGE_CANONICAL_NAME);
    public static final ClassName DATE_UTILS =from(LIGHTBULB_DATE_UTILS_CANONICAL_NAME);
    /**
     * Android
     */
    public static final ClassName ANDROID_FRAGMENT =from(ANDROID_FRAGMENT_CANONICAL_NAME);
    public static final ClassName ANDROID_ACTIVITY = from(ANDROID_ACTIVITY_CANONICAL_NAME);
    public static final ClassName ANDROID_BUNDLE = from(ANDROID_BUNDLE_CANONICAL_NAME);
    public static final ClassName ANDROID_PARCEL = from(ANDROID_PARCEL_CANONICAL_NAME);
    public static final ClassName ANDROID_PARCELABLE = from(ANDROID_PARCELABLE_CANONICAL_NAME);
    public static final ClassName ANDROID_PARCELABLE_CREATOR = from(ANDROID_PARCELABLE_CREATOR_CANONICAL_NAME);
    public static final ClassName ANDROID_SPARSE_ARRAY = from(ANDROID_SPARSE_ARRAY_CANONICAL_NAME);
    public static final ClassName ANDROID_CONTEXT = from(ANDROID_CONTEXT_CANONICAL_NAME);
    public static final ClassName ANDROID_APPLICATION = from(ANDROID_APPLICATION_CANONICAL_NAME);
    public static final ClassName ANDROID_RESOURCES = from(ANDROID_RESOURCES_CANONICAL_NAME);
    public static final ClassName ANDROID_VIEW = from(ANDROID_VIEW_CANONICAL_NAME);
    public static final ClassName ANDROID_LAYOUT_INFLATER = from(ANDROID_LAYOUT_INFLATER_CANONICAL_NAME);
    public static final ClassName ANDROID_VIEW_GROUP = from(ANDROID_VIEW_GROUP_CANONICAL_NAME);
    public static final ClassName ANDROID_SDK_INT = from(ANDROID_SDK_INT_CANONICAL_NAME);
    public static final ClassName ANDROID_DATA_BINDING_UTIL = from(ANDROID_DATA_BINDING_UTIL_CANONICAL_NAME);
    public static final ClassName ANDROID_VIEW_MODEL_PROVIDER = from(ANDROID_VIEW_MODEL_PROVIDER_CANONICAL_NAME);
    /**
     * Internal
     */
    public static final ClassName UUID = ClassName.get(UUID.class);
    public static final ClassName DATE = ClassName.get(Date.class);
    public static final ClassName OFFSET_DATE_TIME = ClassName.get(OffsetDateTime.class);
    public static final ClassName STRING = ClassName.get(String.class);
    public static final ClassName INTEGER = ClassName.get(Integer.class);
    public static final ClassName CLASS = ClassName.get(Class.class);
    public static final ClassName OBJECT = ClassName.get(Object.class);
    public static final ClassName MAP = ClassName.get(Map.class);
    public static final ClassName HASH_MAP = ClassName.get(HashMap.class);
    public static final ClassName ARRAY_LIST = ClassName.get(ArrayList.class);
    public static final ClassName ILLEGAL_ARGUMENT_EXCEPTION = ClassName.get(IllegalArgumentException.class);
    private final PackageNames packageNames;

    public ClassNameUtils(PackageNames packageNames) {
        this.packageNames = packageNames;
    }

    @NotNull
    public ClassName androidResources() {
        return ClassName.get(packageNames.getRootPackage(), "R");
    }

    @NotNull
    public ClassName getClassName(ClassMetadata metadata) {
        return ClassName.get(metadata.getElement());
    }

    @Nullable
    public ClassName getSuperClassName(ClassMetadata metadata) {
        TypeElement superTypeElement = ElementUtils.getSuperType(metadata.getElement());

        if (superTypeElement == null) {
            return null;
        }

        return ClassName.get(superTypeElement);
    }

    @NotNull
    public TypeName getTypeName(TypeInformation typeInformation) {
        return TypeName.get(typeInformation.getTypeMirror());
    }

    @NotNull
    public TypeName getTypeName(BaseMetadata<?> metadata) {
        return TypeName.get(metadata.getTypeMirror());
    }

    public ClassName generateInstrumentedClassName(String classPackage, String className) {
        return generateInstrumentedClassName(classPackage, className, true);
    }

    public ClassName generateInstrumentedClassName(String classPackage, String className, boolean prefix) {
        return generateClassNameWithPrefix(classPackage, className, prefix ? DEFAULT_INSTRUMENTED_CLASS_NAME_PREFIX : "");
    }

    public ClassName generateClassNameWithPrefix(String classPackage, String className, String classNamePrefix) {
        return ClassName.get(classPackage, classNamePrefix.concat(className));
    }

    public ClassName generateVersionCodeClassName(String versionCode) {
        return ClassName.get(ANDROID_BUILD_VERSION_CODES, versionCode);
    }

    public ClassName getFragmentFactoryClassName() {
        return ClassName.get(packageNames.getFragmentsFactoryPackage(), FRAGMENT_FACTORY_CLASS_NAME);
    }

    public ClassName getRoutingScreensClassName() {
        return ClassName.get(packageNames.getRoutingScreensPackage(), ROUTING_SCREENS_CLASS_NAME);
    }

    public ClassName getAppRouterClassName() {
        return ClassName.get(packageNames.getRoutingPackage(), ROUTING_APP_ROUTER_CLASS_NAME);
    }

    public ClassName getLightbulbServiceClassName() {
        return ClassName.get(packageNames.getServicePackage(), LIGHTBULB_SERVICE_CLASS_NAME);
    }

    private static ClassName from(String canonicalName) {
        String packageName = canonicalName.substring(0,canonicalName.lastIndexOf("."));
        String name = canonicalName.substring(canonicalName.lastIndexOf('.') + 1);
        return ClassName.get(packageName,name);
    }
}
