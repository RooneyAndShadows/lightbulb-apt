package com.github.rooneyandshadows.lightbulb.annotation_processors.utils.names;

public class PackageNames {
    private static String rootPackage = null;
    public static final String LB_ROUTING = "com.github.rooneyandshadows.lightbulb.application.activity.routing";
    public static final String LB_ACTIVITY = "com.github.rooneyandshadows.lightbulb.application.activity";
    public static final String LB_FRAGMENT = "com.github.rooneyandshadows.lightbulb.application.fragment.base";
    public static final String ROONEY_AND_SHADOWS_DATE = "com.github.rooneyandshadows.java.commons.date";
    public static final String ANDROID = "android";
    public static final String ANDROIDX_FRAGMENT_APP = "androidx.fragment.app";
    public static final String ANDROID_OS = ANDROID.concat(".os");
    public static final String ANDROID_VIEW = ANDROID.concat(".view");
    public static final String ANDROID_CONTENT_RES = "android.content.res";
    public static final String ANDROID_BUILD_VERSION = ANDROID_OS.concat(".Build.VERSION");
    public static final String ANDROID_BUILD_VERSION_CODES = ANDROID_OS.concat(".Build.VERSION_CODES");

    public static void setRootPackage(String rootPackage) {
        PackageNames.rootPackage = rootPackage;
    }

    public static String getRootPackage() {
        if (rootPackage == null) throw new IllegalStateException("root package cannot be null");
        return PackageNames.rootPackage;
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
