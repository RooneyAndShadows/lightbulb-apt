package com.github.rooneyandshadows.lightbulb.apt.commons;

import org.jetbrains.annotations.NotNull;

import static com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames.*;


public class ClassDefinitions {
    /**
     * Lightbulb
     */
    public static final ClassInfo LIGHTBULB_BASE_ROUTER = new ClassInfo(LB_APT_CORE_ROUTING, "BaseActivityRouter");
    public static final ClassInfo LIGHTBULB_BASE_STORAGE = new ClassInfo(LB_APT_CORE_STORAGE, "BaseStorage");
    public static final ClassInfo LIGHTBULB_DATE_UTILS = new ClassInfo(LB_APT_CORE_UTILS, "DateUtils");
    public static final ClassInfo LIGHTBULB_ANDROID_RESOURCES = new ClassInfo(LB_APT_CORE, "R");
    /**
     * Androids
     */
    public static final ClassInfo PARCEL = new ClassInfo(ANDROID_OS, "Parcel");
    public static final ClassInfo FRAGMENT = new ClassInfo(ANDROIDX_FRAGMENT_APP, "Fragment");
    public static final ClassInfo FRAGMENT_MANAGER = new ClassInfo(ANDROIDX_FRAGMENT_APP, "FragmentManager");
    public static final ClassInfo FRAGMENT_RESULT_LISTENER = new ClassInfo(ANDROIDX_FRAGMENT_APP, "FragmentResultListener");
    public static final ClassInfo ACTIVITY = new ClassInfo(ANDROIDX_APPCOMPAT_APP, "AppCompatActivity");
    public static final ClassInfo BUNDLE = new ClassInfo(ANDROID_OS, "Bundle");
    public static final ClassInfo PARCELABLE =new ClassInfo(ANDROID_OS, "Parcelable");
    public static final ClassInfo PARCELABLE_CREATOR = new ClassInfo(ANDROID_OS, "Parcelable$Creator");
    public static final ClassInfo SPARSE_ARRAY = new ClassInfo(ANDROID_UTIL, "SparseArray");
    public static final ClassInfo CONTEXT =new ClassInfo(ANDROID_CONTENT, "Context");
    public static final ClassInfo APPLICATION = new ClassInfo(ANDROID_APP, "Application");
    public static final ClassInfo RESOURCES =new ClassInfo(ANDROID_CONTENT_RES, "Resources");
    public static final ClassInfo VIEW = new ClassInfo(PackageNames.ANDROID_VIEW, "View");
    public static final ClassInfo LAYOUT_INFLATER = new ClassInfo(PackageNames.ANDROID_VIEW, "LayoutInflater");
    public static final ClassInfo VIEW_GROUP = new ClassInfo(PackageNames.ANDROID_VIEW, "ViewGroup");
    public static final ClassInfo SDK_INT = new ClassInfo(ANDROID_BUILD_VERSION, "SDK_INT");
    public static final ClassInfo DATA_BINDING_UTIL = new ClassInfo(ANDROIDX_DATA_BINDING, "DataBindingUtil");
    public static final ClassInfo VIEW_DATA_BINDING = new ClassInfo(ANDROIDX_DATA_BINDING, "ViewDataBinding");
    public static final ClassInfo VIEW_MODEL_PROVIDER = new ClassInfo(ANDROIDX_LIFECYCLE, "ViewModelProvider");
    public static final ClassInfo VIEW_MODEL = new ClassInfo(ANDROIDX_LIFECYCLE, "ViewModel");


    public static class ClassInfo {
        private final String packageName;
        private final String simpleName;
        private final String cannonicalName;

        public ClassInfo(String packageName, String simpleName) {
            this.packageName = packageName;
            this.simpleName = simpleName;
            this.cannonicalName = resolveCanonical(packageName, simpleName);
        }

        public String getPackageName() {
            return packageName;
        }

        public String getSimpleName() {
            return simpleName;
        }

        public String getCannonicalName() {
            return cannonicalName;
        }

        @Override
        @NotNull
        public String toString() {
            return cannonicalName;
        }

        private static String resolveCanonical(String packageName, String simpleClassName) {
            return String.format("%s.%s", packageName, simpleClassName);
        }
    }
}
