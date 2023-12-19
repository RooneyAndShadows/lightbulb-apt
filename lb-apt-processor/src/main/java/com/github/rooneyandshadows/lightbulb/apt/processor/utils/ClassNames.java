package com.github.rooneyandshadows.lightbulb.apt.processor.utils;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames.*;

public class ClassNames {
    /**
     * Result names
     */
    public static final String DEFAULT_INSTRUMENTED_CLASS_NAME_PREFIX = "Lightbulb_";
    public static final String STORAGE_CLASS_NAME_PREFIX = "App";
    public static final String FRAGMENT_FACTORY_CLASS_NAME = "Fragments";
    public static final String ROUTING_SCREENS_CLASS_NAME = "Screens";
    public static final String ROUTING_APP_ROUTER_CLASS_NAME = "AppRouter";
    public static final String LIGHTBULB_SERVICE_CLASS_NAME = "LightbulbService";
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
    public static final ClassName ANDROID_CONTEXT = ClassName.get(ANDROID_CONTENT, "Context");
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
    public static final ClassName ILLEGAL_ARGUMENT_EXCEPTION = ClassName.get(IllegalArgumentException.class);


    public static ClassName androidResources() {
        return ClassName.get(PackageNames.getRootPackage(), "R");
    }

    public static String getClassPackage(Elements elements, TypeElement element) {
        return ElementUtils.getPackage(elements, element);
    }

    public static ClassName generateClassNameWithSuffix(String classPackage, String className, String classNameSuffix) {
        return ClassName.get(classPackage, className.concat(classNameSuffix));
    }

    public static ClassName generateClassNameWithPrefix(String classPackage, String className, String classNamePrefix) {
        return ClassName.get(classPackage, classNamePrefix.concat(className));
    }

    public static ClassName generateClassName(TypeElement element, Elements elements) {
        String classPackage = ElementUtils.getPackage(elements, element);
        return ClassName.get(classPackage, element.getSimpleName().toString());
    }

    public static ClassName generateSuperClassName(TypeElement element, Elements elements) {
        TypeElement superClassElement = ElementUtils.getSuperType(element);
        return generateClassName(superClassElement, elements);
    }

    public static ClassName generateVersionCodeClassName(String versionCode) {
        return ClassName.get(ANDROID_BUILD_VERSION_CODES, versionCode);
    }

    public static ClassName getFragmentFactoryClassName() {
        return ClassName.get(PackageNames.getFragmentsFactoryPackage(), FRAGMENT_FACTORY_CLASS_NAME);
    }

    public static ClassName getRoutingScreensClassName() {
        return ClassName.get(PackageNames.getRoutingScreensPackage(), ROUTING_SCREENS_CLASS_NAME);
    }

    public static ClassName getAppRouterClassName() {
        return ClassName.get(PackageNames.getRoutingPackage(), ROUTING_APP_ROUTER_CLASS_NAME);
    }

    public static ClassName getLightbulbServiceClassName() {
        return ClassName.get(PackageNames.getServicePackage(), LIGHTBULB_SERVICE_CLASS_NAME);
    }
}
