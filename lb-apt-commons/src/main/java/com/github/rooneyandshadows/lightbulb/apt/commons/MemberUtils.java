package com.github.rooneyandshadows.lightbulb.apt.commons;

public class MemberUtils {

    public static String getFieldNameForClass(String className) {
        return lowerCaseFirstLetter(className);
    }

    public static String getFieldSetterName(String fieldName) {
        return String.format("set%s",capitalizeFirstLetter(fieldName));
    }

    public static String getFieldGetterName(String fieldName) {
        return String.format("get%s",capitalizeFirstLetter(fieldName));
    }

    private static String capitalizeFirstLetter(String target) {
        return target.substring(0, 1).toUpperCase() + target.substring(1);
    }

    private static String lowerCaseFirstLetter(String target) {
        return target.substring(0, 1).toLowerCase() + target.substring(1);
    }
}
