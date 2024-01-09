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

import static com.github.rooneyandshadows.lightbulb.apt.commons.GeneratedClassNames.*;
import static com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames.*;


public class ClassNames {
    public static final String ANDROID_PARCEL_SIMPLE_NAME = "Parcel";
    public static final String ANDROID_PARCEL_CANONICAL_NAME = resolveCanonical(ANDROID_OS, ANDROID_PARCEL_SIMPLE_NAME);
    /**
     * Lightbulb
     */
    public static final ClassName BASE_ROUTER = ClassName.get(LB_APT_CORE_ROUTING, "BaseActivityRouter");
    public static final ClassName BASE_STORAGE = ClassName.get(LB_APT_CORE_STORAGE, "BaseStorage");
    public static final ClassName DATE_UTILS = ClassName.get(LB_APT_CORE_UTILS, "DateUtils");
    public static final ClassName LB_TRANSFORMATION_ANNOTATION = ClassName.get(LB_APT_ANNOTATIONS, "LightbulbTransformation");
    /**
     * Android
     */
    public static final ClassName ANDROID_FRAGMENT = ClassName.get(ANDROIDX_FRAGMENT_APP, "Fragment");
    public static final ClassName ANDROID_ACTIVITY = ClassName.get(ANDROIDX_APPCOMPAT_APP, "AppCompatActivity");
    public static final ClassName ANDROID_BUNDLE = ClassName.get(ANDROID_OS, "Bundle");
    public static final ClassName ANDROID_PARCEL = ClassName.get(ANDROID_OS, ANDROID_PARCEL_SIMPLE_NAME);
    public static final ClassName ANDROID_PARCELABLE = ClassName.get(ANDROID_OS, "Parcelable");
    public static final ClassName ANDROID_PARCELABLE_CREATOR = ClassName.get(ANDROID_OS, "Parcelable", "Creator");
    public static final ClassName ANDROID_SPARSE_ARRAY = ClassName.get(ANDROID_UTIL, "SparseArray");
    public static final ClassName ANDROID_CONTEXT = ClassName.get(ANDROID_CONTENT, "Context");
    public static final ClassName ANDROID_APPLICATION = ClassName.get(ANDROID_APP, "Application");
    public static final ClassName ANDROID_RESOURCES = ClassName.get(ANDROID_CONTENT_RES, "Resources");
    public static final ClassName ANDROID_VIEW = ClassName.get(PackageNames.ANDROID_VIEW, "View");
    public static final ClassName ANDROID_LAYOUT_INFLATER = ClassName.get(PackageNames.ANDROID_VIEW, "LayoutInflater");
    public static final ClassName ANDROID_VIEW_GROUP = ClassName.get(PackageNames.ANDROID_VIEW, "ViewGroup");
    public static final ClassName ANDROID_SDK_INT = ClassName.get(ANDROID_BUILD_VERSION, "SDK_INT");
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

    public ClassNames(PackageNames packageNames) {
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

    private static String resolveCanonical(String packageName, String simpleClassName) {
        return String.format("%s.%s", packageName, simpleClassName);
    }
}
