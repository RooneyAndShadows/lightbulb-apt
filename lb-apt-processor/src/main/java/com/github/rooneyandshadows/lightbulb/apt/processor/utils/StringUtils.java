package com.github.rooneyandshadows.lightbulb.apt.processor.utils;

public class StringUtils {
    public static String capitalizeFirstLetter(String target) {
        return target.substring(0, 1).toUpperCase() + target.substring(1);
    }

    public static String lowerCaseFirstLetter(String target) {
        return target.substring(0, 1).toLowerCase() + target.substring(1);
    }
}