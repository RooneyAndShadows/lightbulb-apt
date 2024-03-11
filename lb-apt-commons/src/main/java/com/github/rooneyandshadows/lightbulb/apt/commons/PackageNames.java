package com.github.rooneyandshadows.lightbulb.apt.commons;

@SuppressWarnings("SpellCheckingInspection")
public class PackageNames {
    /**
     * Lightbulb
     */
    public static final String ROONEY_AND_SHADOWS = "com.github.rooneyandshadows";
    public static final String LB_APT_CORE = resolve(ROONEY_AND_SHADOWS, "lightbulb", "apt", "android", "core");
    public static final String LB_APT_CORE_ROUTING = resolve(LB_APT_CORE, "routing");
    public static final String LB_APT_CORE_STORAGE = resolve(LB_APT_CORE, "storage");
    public static final String LB_APT_CORE_UTILS = resolve(LB_APT_CORE, "utils");
    public static final String LB_APT_PROCESSOR = resolve(ROONEY_AND_SHADOWS, "lightbulb", "apt", "processor");
    public static final String LB_APT_ANNOTATIONS = resolve(LB_APT_PROCESSOR, "annotations");
    /**
     * Android
     */
    public static final String ANDROID = "android";
    public static final String ANDROIDX = "androidx";
    public static final String ANDROID_OS = resolve(ANDROID, "os");
    public static final String ANDROID_UTIL = resolve(ANDROID, "util");
    public static final String ANDROID_VIEW = resolve(ANDROID, "view");
    public static final String ANDROID_APP = resolve(ANDROID, "app");
    public static final String ANDROID_CONTENT = resolve(ANDROID, "content");
    public static final String ANDROID_CONTENT_RES = resolve(ANDROID_CONTENT, "res");
    public static final String ANDROIDX_FRAGMENT_APP = resolve(ANDROIDX, "fragment", "app");
    public static final String ANDROIDX_APPCOMPAT_APP = resolve(ANDROIDX, "appcompat", "app");
    public static final String ANDROIDX_DATA_BINDING = resolve(ANDROIDX, "databinding");
    public static final String ANDROIDX_LIFECYCLE = resolve(ANDROIDX, "lifecycle");
    public static final String ANDROID_BUILD_VERSION = resolve(ANDROID_OS, "Build", "VERSION");
    public static final String ANDROID_BUILD_VERSION_CODES = resolve(ANDROID_OS, "Build", "VERSION_CODES");
    private final String rootPackage;

    public PackageNames(String rootPackage) {
        this.rootPackage = rootPackage;
    }

    public String getRootPackage() {
        return rootPackage;
    }

    public String getLightbulbPackage() {
        return resolve(rootPackage, "lightbulb");
    }

    public String getComponentsPackage() {
        return resolve(getLightbulbPackage(), "components");
    }

    public String getRoutingPackage() {
        return resolve(getLightbulbPackage(), "routing");
    }

    public String getRoutingScreensPackage() {
        return resolve(getRoutingPackage(), "screens");
    }

    public String getStoragePackage() {
        return resolve(getLightbulbPackage(), "storage");
    }

    public String getParcelablePackage() {
        return resolve(getLightbulbPackage(), "parcelable");
    }

    public String getServicePackage() {
        return resolve(getLightbulbPackage(), "service");
    }

    public String getFragmentsPackage() {
        return resolve(getLightbulbPackage(), "fragments");
    }

    public String getFragmentsResultPackage() {
        return resolve(getFragmentsPackage(), "result");
    }

    public String getFragmentsFactoryPackage() {
        return resolve(getFragmentsPackage(), "factory");
    }

    public String getActivitiesPackage() {
        return resolve(getLightbulbPackage(), "activities");
    }

    public String getApplicationPackage() {
        return resolve(getLightbulbPackage(), "application");
    }

    private static String resolve(String first, String... other) {
        String packageString = first;
        for (String packagePart : other) {
            packageString = packageString.concat(".").concat(packagePart);
        }
        return packageString;
    }
}
