package com.github.rooneyandshadows.lightbulb.apt.processor.utils;

import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TypeUtils {
    public static final String stringType = String.class.getCanonicalName();
    public static final String intType = Integer.class.getCanonicalName();
    public static final String intPrimType = int.class.getCanonicalName();
    public static final String booleanType = Boolean.class.getCanonicalName();
    public static final String booleanPrimType = boolean.class.getCanonicalName();
    public static final String uuidType = java.util.UUID.class.getCanonicalName();
    public static final String floatType = Float.class.getCanonicalName();
    public static final String floatPrimType = float.class.getCanonicalName();
    public static final String longType = Long.class.getCanonicalName();
    public static final String longPrimType = long.class.getCanonicalName();
    public static final String doubleType = Double.class.getCanonicalName();
    public static final String doublePrimType = double.class.getCanonicalName();
    public static final String dateType = Date.class.getCanonicalName();
    public static final String offsetDateType = OffsetDateTime.class.getCanonicalName();
    private static final List<String> simpleTypesList = Arrays.asList(stringType, intType, intPrimType, booleanType,
            booleanPrimType, uuidType, floatType, floatPrimType, longType, longPrimType, doubleType, doublePrimType);

    public static boolean isSimpleType(String canonicalName) {
        return simpleTypesList.contains(canonicalName);
    }

    public static boolean isString(String target) {
        return target.equals(stringType);
    }

    public static boolean isUUID(String target) {
        return target.equals(uuidType);
    }

    public static boolean isDate(String target) {
        return target.equals(dateType);
    }

    public static boolean isOffsetDate(String target) {
        return target.equals(offsetDateType);
    }

    public static boolean isInt(String target) {
        return target.equals(intType) || target.equals(intPrimType);
    }

    public static boolean isBoolean(String target) {
        return target.equals(booleanType) || target.equals(booleanPrimType);
    }

    public static boolean isFloat(String target) {
        return target.equals(floatType) || target.equals(floatPrimType);
    }

    public static boolean isLong(String target) {
        return target.equals(longType) || target.equals(longPrimType);
    }

    public static boolean isDouble(String target) {
        return target.equals(doubleType) || target.equals(doublePrimType);
    }

    public static boolean isFromType(TypeName requestType, TypeName expectedType) {
        if(requestType instanceof ParameterizedTypeName) {
            TypeName typeName = ((ParameterizedTypeName) requestType).rawType;
            return (typeName.equals(expectedType));
        }

        return false;
    }
}
