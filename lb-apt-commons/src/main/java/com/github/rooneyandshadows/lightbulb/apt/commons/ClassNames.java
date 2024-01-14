package com.github.rooneyandshadows.lightbulb.apt.commons;

import static com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames.*;


public class ClassNames {
    /**
     * Lightbulb
     */
    public static final String LIGHTBULB_BASE_ROUTER_SIMPLE_NAME = "BaseActivityRouter";
    public static final String LIGHTBULB_BASE_ROUTER_CANONICAL_NAME = resolveCanonical(LB_APT_CORE_ROUTING, LIGHTBULB_BASE_ROUTER_SIMPLE_NAME);
    public static final String LIGHTBULB_BASE_STORAGE_SIMPLE_NAME = "BaseStorage";
    public static final String LIGHTBULB_BASE_STORAGE_CANONICAL_NAME = resolveCanonical(LB_APT_CORE_STORAGE, LIGHTBULB_BASE_STORAGE_SIMPLE_NAME);
    public static final String LIGHTBULB_DATE_UTILS_SIMPLE_NAME = "DateUtils";
    public static final String LIGHTBULB_DATE_UTILS_CANONICAL_NAME = resolveCanonical(LB_APT_CORE_UTILS, LIGHTBULB_DATE_UTILS_SIMPLE_NAME);
    /**
     * Android
     */
    public static final String ANDROID_PARCEL_SIMPLE_NAME = "Parcel";
    public static final String ANDROID_PARCEL_CANONICAL_NAME = resolveCanonical(ANDROID_OS, ANDROID_PARCEL_SIMPLE_NAME);
    public static final String ANDROID_FRAGMENT_SIMPLE_NAME = "Fragment";
    public static final String ANDROID_FRAGMENT_CANONICAL_NAME = resolveCanonical(ANDROIDX_FRAGMENT_APP, ANDROID_FRAGMENT_SIMPLE_NAME);
    public static final String ANDROID_ACTIVITY_SIMPLE_NAME = "AppCompatActivity";
    public static final String ANDROID_ACTIVITY_CANONICAL_NAME = resolveCanonical(ANDROIDX_APPCOMPAT_APP, ANDROID_ACTIVITY_SIMPLE_NAME);
    public static final String ANDROID_BUNDLE_SIMPLE_NAME = "Bundle";
    public static final String ANDROID_BUNDLE_CANONICAL_NAME = resolveCanonical(ANDROID_OS, ANDROID_BUNDLE_SIMPLE_NAME);
    public static final String ANDROID_PARCELABLE_SIMPLE_NAME = "Parcelable";
    public static final String ANDROID_PARCELABLE_CANONICAL_NAME = resolveCanonical(ANDROID_OS, ANDROID_PARCELABLE_SIMPLE_NAME);
    public static final String ANDROID_PARCELABLE_CREATOR_SIMPLE_NAME = "Parcelable$Creator";
    public static final String ANDROID_PARCELABLE_CREATOR_CANONICAL_NAME = resolveCanonical(ANDROID_OS, ANDROID_PARCELABLE_CREATOR_SIMPLE_NAME);
    public static final String ANDROID_SPARSE_ARRAY_SIMPLE_NAME = "SparseArray";
    public static final String ANDROID_SPARSE_ARRAY_CANONICAL_NAME = resolveCanonical(ANDROID_UTIL, ANDROID_SPARSE_ARRAY_SIMPLE_NAME);
    public static final String ANDROID_CONTEXT_SIMPLE_NAME = "Context";
    public static final String ANDROID_CONTEXT_CANONICAL_NAME = resolveCanonical(ANDROID_CONTENT, ANDROID_CONTEXT_SIMPLE_NAME);
    public static final String ANDROID_APPLICATION_SIMPLE_NAME = "Application";
    public static final String ANDROID_APPLICATION_CANONICAL_NAME = resolveCanonical(ANDROID_APP, ANDROID_APPLICATION_SIMPLE_NAME);
    public static final String ANDROID_RESOURCES_SIMPLE_NAME = "Resources";
    public static final String ANDROID_RESOURCES_CANONICAL_NAME = resolveCanonical(ANDROID_CONTENT_RES, ANDROID_RESOURCES_SIMPLE_NAME);
    public static final String ANDROID_VIEW_SIMPLE_NAME = "View";
    public static final String ANDROID_VIEW_CANONICAL_NAME = resolveCanonical(PackageNames.ANDROID_VIEW, ANDROID_VIEW_SIMPLE_NAME);
    public static final String ANDROID_LAYOUT_INFLATER_SIMPLE_NAME = "LayoutInflater";
    public static final String ANDROID_LAYOUT_INFLATER_CANONICAL_NAME = resolveCanonical(PackageNames.ANDROID_VIEW, ANDROID_LAYOUT_INFLATER_SIMPLE_NAME);
    public static final String ANDROID_VIEW_GROUP_SIMPLE_NAME = "ViewGroup";
    public static final String ANDROID_VIEW_GROUP_CANONICAL_NAME = resolveCanonical(PackageNames.ANDROID_VIEW, ANDROID_VIEW_GROUP_SIMPLE_NAME);
    public static final String ANDROID_SDK_INT_SIMPLE_NAME = "SDK_INT";
    public static final String ANDROID_SDK_INT_CANONICAL_NAME = resolveCanonical(ANDROID_BUILD_VERSION, ANDROID_SDK_INT_SIMPLE_NAME);
    public static final String ANDROID_DATA_BINDING_UTIL_SIMPLE_NAME = "DataBindingUtil";
    public static final String ANDROID_DATA_BINDING_UTIL_CANONICAL_NAME = resolveCanonical(ANDROIDX_DATA_BINDING, ANDROID_DATA_BINDING_UTIL_SIMPLE_NAME);
    public static final String ANDROID_VIEW_DATA_BINDING_SIMPLE_NAME = "ViewDataBinding";
    public static final String ANDROID_VIEW_DATA_BINDING_CANONICAL_NAME = resolveCanonical(ANDROIDX_DATA_BINDING, ANDROID_VIEW_DATA_BINDING_SIMPLE_NAME);
    public static final String ANDROID_VIEW_MODEL_PROVIDER_SIMPLE_NAME = "ViewModelProvider";
    public static final String ANDROID_VIEW_MODEL_PROVIDER_CANONICAL_NAME = resolveCanonical(ANDROIDX_LIFECYCLE, ANDROID_VIEW_MODEL_PROVIDER_SIMPLE_NAME);

    private static String resolveCanonical(String packageName, String simpleClassName) {
        return String.format("%s.%s", packageName, simpleClassName);
    }
}
