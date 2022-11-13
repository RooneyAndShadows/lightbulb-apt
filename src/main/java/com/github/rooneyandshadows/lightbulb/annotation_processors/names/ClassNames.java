package com.github.rooneyandshadows.lightbulb.annotation_processors.names;

import com.github.rooneyandshadows.lightbulb.annotation_processors.fragment.FragmentInfo;
import com.github.rooneyandshadows.lightbulb.annotation_processors.utils.ElementUtils;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;

import static com.github.rooneyandshadows.lightbulb.annotation_processors.names.PackageNames.*;

public class ClassNames {
    public static final ClassName BASE_FRAGMENT = ClassName.get(LB_FRAGMENT, "BaseFragment");
    public static final ClassName BASE_FRAGMENT_CONFIGURATION = BASE_FRAGMENT.nestedClass("Configuration");
    public static final ClassName BASE_ACTIVITY = ClassName.get(LB_ACTIVITY, "BaseActivity");
    public static final ClassName BASE_ROUTER = ClassName.get(LB_ROUTING, "BaseActivityRouter");
    public static final ClassName DATE_UTILS = ClassName.get(ROONEY_AND_SHADOWS_DATE, "DateUtils");
    public static final ClassName OFFSET_DATE_UTILS = ClassName.get(ROONEY_AND_SHADOWS_DATE, "DateUtilsOffsetDate");
    public static final ClassName BUNDLE = ClassName.get(ANDROID_OS, "Bundle");
    public static final ClassName GENERATED_SCREENS = ClassName.get(GENERATED_LB_SCREENS, "Screens");
    public static final ClassName UUID = ClassName.get(JAVA_UTIL, "UUID");
    public static final ClassName DATE = ClassName.get(JAVA_UTIL, "Date");
    public static final ClassName OFFSET_DATE_TIME = ClassName.get(JAVA_TIME, "OffsetDateTime");
    public static final ClassName STRING = ClassName.get(JAVA_LANG, "String");

    public static ClassName generateMappedFragmentBindingClassName(FragmentInfo fragmentInfo, String className) {
        return ClassName.get(fragmentInfo.getClassName().packageName(), className);
    }

    public static ClassName generateFragmentClassName(Element fragmentClass, Elements elements) {
        String classPackage = ElementUtils.getPackage(elements, fragmentClass);
        return ClassName.get(classPackage, fragmentClass.getSimpleName().toString());
    }
}
