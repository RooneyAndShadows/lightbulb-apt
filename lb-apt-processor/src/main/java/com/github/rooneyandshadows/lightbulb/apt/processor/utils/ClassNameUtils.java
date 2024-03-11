package com.github.rooneyandshadows.lightbulb.apt.processor.utils;

import com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.ClassMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.MethodMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.TypedMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.TypeDefinition;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.*;

import static com.github.rooneyandshadows.lightbulb.apt.commons.ClassDefinitions.*;
import static com.github.rooneyandshadows.lightbulb.apt.commons.GeneratedClassNames.*;
import static com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames.ANDROID_BUILD_VERSION_CODES;


public class ClassNameUtils {
    /**
     * Lightbulb
     */
    public static final ClassName BASE_ROUTER = from(LIGHTBULB_BASE_ROUTER);
    public static final ClassName BASE_STORAGE = from(LIGHTBULB_BASE_STORAGE);
    public static final ClassName DATE_UTILS = from(LIGHTBULB_DATE_UTILS);
    public static final ClassName LB_ANDROID_RESOURCES = from(LIGHTBULB_ANDROID_RESOURCES);
    /**
     * Android
     */
    public static final ClassName ANDROID_FRAGMENT = from(FRAGMENT);
    public static final ClassName ANDROID_FRAGMENT_MANAGER = from(FRAGMENT_MANAGER);
    public static final ClassName ANDROID_FRAGMENT_RESULT_LISTENER = from(FRAGMENT_RESULT_LISTENER);
    public static final ClassName ANDROID_ACTIVITY = from(ACTIVITY);
    public static final ClassName ANDROID_BUNDLE = from(BUNDLE);
    public static final ClassName ANDROID_PARCEL = from(PARCEL);
    public static final ClassName ANDROID_PARCELABLE = from(PARCELABLE);
    public static final ClassName ANDROID_PARCELABLE_CREATOR = from(PARCELABLE_CREATOR);
    public static final ClassName ANDROID_SPARSE_ARRAY = from(SPARSE_ARRAY);
    public static final ClassName ANDROID_CONTEXT = from(CONTEXT);
    public static final ClassName ANDROID_APPLICATION = from(APPLICATION);
    public static final ClassName ANDROID_RESOURCES = from(RESOURCES);
    public static final ClassName ANDROID_VIEW = from(VIEW);
    public static final ClassName ANDROID_LAYOUT_INFLATER = from(LAYOUT_INFLATER);
    public static final ClassName ANDROID_VIEW_GROUP = from(VIEW_GROUP);
    public static final ClassName ANDROID_SDK_INT = from(SDK_INT);
    public static final ClassName ANDROID_DATA_BINDING_UTIL = from(DATA_BINDING_UTIL);
    public static final ClassName ANDROID_VIEW_MODEL_PROVIDER = from(VIEW_MODEL_PROVIDER);
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
    public ClassName localAndroidResources() {
        return ClassName.get(packageNames.getRootPackage(), "R");
    }

    @NotNull
    public ClassName getClassName(ClassMetadata metadata) {
        return from(metadata.getQualifiedName());
    }

    @Nullable
    public ClassName getSuperClassName(ClassMetadata metadata) {
        TypeDefinition superTypeDefinition = metadata.getType().getSuperClassType();

        if (superTypeDefinition == null) {
            return null;
        }

        return from(superTypeDefinition.getQualifiedName());
    }

    @NotNull
    public TypeName getTypeName(TypeDefinition typeInformation) {
        return TypeName.get(typeInformation.getTypeMirror());
    }

    @NotNull
    public TypeName getTypeName(TypedMetadata metadata) {
        return TypeName.get(metadata.getType().getTypeMirror());
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

    public ClassName getFragmentResultClassName() {
        return ClassName.get(packageNames.getFragmentsResultPackage(), FRAGMENT_RESULT_CLASS_NAME);
    }

    public ClassName getLightbulbServiceClassName() {
        return ClassName.get(packageNames.getServicePackage(), LIGHTBULB_SERVICE_CLASS_NAME);
    }

    private static ClassName from(ClassInfo classInfo) {
        return from(classInfo.getCannonicalName());
    }

    private static ClassName from(String canonicalName) {
        String packageName = canonicalName.substring(0, canonicalName.lastIndexOf("."));
        String name = canonicalName.substring(canonicalName.lastIndexOf('.') + 1);
        return ClassName.get(packageName, name);
    }
}
