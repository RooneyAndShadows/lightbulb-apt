package com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names;

import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names.PackageNames.*;

public class ClassNames {
    public static final ClassName BASE_ACTIVITY = ClassName.get(LB_ACTIVITY, "BaseActivity");
    public static final ClassName BASE_ROUTER = ClassName.get(LB_ROUTING, "BaseActivityRouter");
    public static final ClassName DATE_UTILS = ClassName.get(ROONEY_AND_SHADOWS_DATE, "DateUtils");
    public static final ClassName OFFSET_DATE_UTILS = ClassName.get(ROONEY_AND_SHADOWS_DATE, "DateUtilsOffsetDate");
    public static final ClassName ANDROID_FRAGMENT = ClassName.get(ANDROIDX_FRAGMENT_APP, "Fragment");
    public static final ClassName ANDROID_BUNDLE = ClassName.get(ANDROID_OS, "Bundle");
    public static final ClassName ANDROID_RESOURCES = ClassName.get(ANDROID_CONTENT_RES, "Resources");
    public static final ClassName ANDROID_VIEW = ClassName.get(PackageNames.ANDROID_VIEW, "View");
    public static final ClassName ANDROID_LAYOUT_INFLATER = ClassName.get(PackageNames.ANDROID_VIEW, "LayoutInflater");
    public static final ClassName ANDROID_VIEW_GROUP = ClassName.get(PackageNames.ANDROID_VIEW, "ViewGroup");
    public static final ClassName SDK_INT = ClassName.get(ANDROID_BUILD_VERSION, "SDK_INT");
    public static final ClassName UUID = ClassName.get(UUID.class);
    public static final ClassName DATE = ClassName.get(Date.class);
    public static final ClassName OFFSET_DATE_TIME = ClassName.get(OffsetDateTime.class);
    public static final ClassName STRING = ClassName.get(String.class);
    public static final ClassName CLASS = ClassName.get(Class.class);
    public static final ClassName OBJECT = ClassName.get(Object.class);
    public static final ClassName MAP = ClassName.get(Map.class);
    public static final ClassName HASH_MAP = ClassName.get(HashMap.class);
    public static final ClassName ILLEGAL_ARGUMENT_EXCEPTION = ClassName.get(IllegalArgumentException.class);
    public static final String FRAGMENT_FACTORY_CLASS_NAME = "Fragments";


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
}
