package com.github.rooneyandshadows.lightbulb.apt.processor.utils;

public class PackageNames {
    private static String rootPackage = null;
    /**
     * Lightbulb
     */
    public static final String ROONEY_AND_SHADOWS = "com.github.rooneyandshadows";
    public static final String ROONEY_AND_SHADOWS_DATE = resolve(ROONEY_AND_SHADOWS, "date");
    public static final String LB_APT_CORE = resolve(ROONEY_AND_SHADOWS, "lightbulb", "apt", "android", "core");
    public static final String LB_APT_CORE_ROUTING = resolve(LB_APT_CORE, "routing");
    public static final String LB_APT_CORE_UTILS = resolve(LB_APT_CORE, "utils");
    /**
     * Android
     */
    public static final String ANDROID = "android";
    public static final String ANDROIDX = "androidx";
    public static final String ANDROID_OS = resolve(ANDROID, "os");
    public static final String ANDROID_VIEW = resolve(ANDROID, "view");
    public static final String ANDROID_CONTENT_RES = resolve(ANDROID, "content", "res");
    public static final String ANDROIDX_FRAGMENT_APP = resolve(ANDROIDX, "fragment", "app");
    public static final String ANDROIDX_APPCOMPAT_APP = resolve(ANDROIDX, "appcompat", "app");
    public static final String ANDROID_BUILD_VERSION = resolve(ANDROID_OS, "Build", "VERSION");
    public static final String ANDROID_BUILD_VERSION_CODES = resolve(ANDROID_OS, "Build", "VERSION_CODES");

    public static void init(String projectRoot) {
        System.out.println("==================================="+projectRoot);
        rootPackage = projectRoot;
    }

    public static String getRootPackage() {
        System.out.println("sssssssssssssssssssssssssssssssssssssssss");
        if (rootPackage == null) throw new IllegalStateException("root package cannot be null");
        return rootPackage;
    }

    public static String getRoutingPackage() {
        return resolve(getRootPackage(), "routing");
    }

    public static String getRoutingScreensPackage() {
        return resolve(getRootPackage(), "routing", "screens");
    }

    public static String getFragmentsFactoryPackage() {
        return resolve(getRootPackage(), "fragments", "factory");
    }

    public static String getFragmentsPackage() {
        return resolve(getRootPackage(), "fragments");
    }

    public static String resolve(String first, String... other) {
        String packageString = first;
        for (String packagePart : other) {
            packageString = packageString.concat(".").concat(packagePart);
        }
        return packageString;
    }
}
