package com.github.rooneyandshadows.lightbulb.apt.processor.generator.base;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.*;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.DATE_UTILS;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.TypeUtils.*;

public class BundleCodeGenerator {

    public static void generateWriteStatement(TypeName paramType, CodeBlock.Builder cbBuilder, String bundleVariableName, String variableAccessor, String key) {
        String typeString = paramType.toString();
        boolean checkForNull = !paramType.isPrimitive();

        if (checkForNull) {
            cbBuilder.beginControlFlow("if($L != null)", variableAccessor);
        }

        if (isString(typeString)) writeString(cbBuilder, bundleVariableName, variableAccessor, key);
        else if (isUUID(typeString)) writeUUID(cbBuilder, bundleVariableName, variableAccessor, key);
        else if (isInt(typeString)) writeInt(cbBuilder, bundleVariableName, variableAccessor, key);
        else if (isBoolean(typeString)) writeBoolean(cbBuilder, bundleVariableName, variableAccessor, key);
        else if (isFloat(typeString)) writeFloat(cbBuilder, bundleVariableName, variableAccessor, key);
        else if (isLong(typeString)) writeLong(cbBuilder, bundleVariableName, variableAccessor, key);
        else if (isDouble(typeString)) writeDouble(cbBuilder, bundleVariableName, variableAccessor, key);
        else if (isDate(typeString)) writeDate(cbBuilder, bundleVariableName, variableAccessor, key);
        else if (isOffsetDate(typeString)) writeOffsetDate(cbBuilder, bundleVariableName, variableAccessor, key);
        else writeParcelable(cbBuilder, bundleVariableName, variableAccessor, key);

        if (checkForNull) {
            cbBuilder.endControlFlow();
        }
    }

    public static void generateReadStatement(TypeName paramType, CodeBlock.Builder cbBuilder, String bundleVariableName, String variableName, String key) {
        String typeString = paramType.toString();

        if (isString(typeString)) readString(cbBuilder, paramType, bundleVariableName, variableName, key);
        else if (isUUID(typeString)) readUUID(cbBuilder, paramType, bundleVariableName, variableName, key);
        else if (isInt(typeString)) readInt(cbBuilder, paramType, bundleVariableName, variableName, key);
        else if (isBoolean(typeString)) readBoolean(cbBuilder, paramType, bundleVariableName, variableName, key);
        else if (isFloat(typeString)) readFloat(cbBuilder, paramType, bundleVariableName, variableName, key);
        else if (isLong(typeString)) readLong(cbBuilder, paramType, bundleVariableName, variableName, key);
        else if (isDouble(typeString)) readDouble(cbBuilder, paramType, bundleVariableName, variableName, key);
        else if (isDate(typeString)) readDate(cbBuilder, paramType, bundleVariableName, variableName, key);
        else if (isOffsetDate(typeString)) readOffsetDate(cbBuilder, paramType, bundleVariableName, variableName, key);
        else readParcelable(cbBuilder, paramType, bundleVariableName, variableName, key);
    }

    private static void readString(CodeBlock.Builder cbBuilder, TypeName paramType, String bundleVariableName, String variableName, String key) {
        String bundleExp = "$T $L = ".concat(String.format("%s.getString($S,\"\")", bundleVariableName));
        cbBuilder.addStatement(bundleExp, paramType, variableName, key);
    }

    private static void writeString(CodeBlock.Builder cbBuilder, String bundleVariableName, String variableAccessor, String key) {
        cbBuilder.addStatement("$L.putString($S,$L)", bundleVariableName, key, variableAccessor);
    }

    private static void readUUID(CodeBlock.Builder cbBuilder, TypeName paramType, String bundleVariableName, String variableName, String key) {
        String bundleExp = String.format("$T $L = $T.fromString(%s.getString($S,\"\"))", bundleVariableName);
        cbBuilder.addStatement(bundleExp, paramType, variableName, paramType, key);
    }

    private static void writeUUID(CodeBlock.Builder cbBuilder, String bundleVariableName, String variableAccessor, String key) {
        cbBuilder.addStatement("$L.putString($S,$L.toString())", bundleVariableName, key, variableAccessor);
    }

    private static void readInt(CodeBlock.Builder cbBuilder, TypeName paramType, String bundleVariableName, String variableName, String key) {
        String bundleExp = "$T $L = ".concat(String.format("%s.getInt($S)", bundleVariableName));
        cbBuilder.addStatement(bundleExp, paramType, variableName, key);
    }

    private static void writeInt(CodeBlock.Builder cbBuilder, String bundleVariableName, String variableAccessor, String key) {
        cbBuilder.addStatement("$L.putInt($S,$L)", bundleVariableName, key, variableAccessor);
    }

    private static void readBoolean(CodeBlock.Builder cbBuilder, TypeName paramType, String bundleVariableName, String variableName, String key) {
        String bundleExp = "$T $L = ".concat(String.format("%s.getBoolean($S)", bundleVariableName));
        cbBuilder.addStatement(bundleExp, paramType, variableName, key);
    }

    private static void writeBoolean(CodeBlock.Builder cbBuilder, String bundleVariableName, String variableAccessor, String key) {
        cbBuilder.addStatement("$L.putBoolean($S,$L)", bundleVariableName, key, variableAccessor);
    }

    private static void readFloat(CodeBlock.Builder cbBuilder, TypeName paramType, String bundleVariableName, String variableName, String key) {
        String bundleExp = "$T $L = ".concat(String.format("%s.getFloat($S)", bundleVariableName));
        cbBuilder.addStatement(bundleExp, paramType, variableName, key);
    }

    private static void writeFloat(CodeBlock.Builder cbBuilder, String bundleVariableName, String variableAccessor, String key) {
        cbBuilder.addStatement("$L.putFloat($S,$L)", bundleVariableName, key, variableAccessor);
    }

    private static void readLong(CodeBlock.Builder cbBuilder, TypeName paramType, String bundleVariableName, String variableName, String key) {
        String bundleExp = "$T $L = ".concat(String.format("%s.getLong($S)", bundleVariableName));
        cbBuilder.addStatement(bundleExp, paramType, variableName, key);
    }

    private static void writeLong(CodeBlock.Builder cbBuilder, String bundleVariableName, String variableAccessor, String key) {
        cbBuilder.addStatement("$L.putLong($S,$L)", bundleVariableName, key, variableAccessor);
    }

    private static void readDouble(CodeBlock.Builder cbBuilder, TypeName paramType, String bundleVariableName, String variableName, String key) {
        String bundleExp = "$T $L = ".concat(String.format("%s.getDouble($S)", bundleVariableName));
        cbBuilder.addStatement(bundleExp, paramType, variableName, key);
    }

    private static void writeDouble(CodeBlock.Builder cbBuilder, String bundleVariableName, String variableAccessor, String key) {
        cbBuilder.addStatement("$L.putDouble($S,$L)", bundleVariableName, key, variableAccessor);
    }

    private static void readDate(CodeBlock.Builder cbBuilder, TypeName paramType, String bundleVariableName, String variableName, String key) {
        String tmpVarName = key.concat("DateString");
        String getDateStringExpression = String.format("%s.getString($S)", bundleVariableName);
        cbBuilder.addStatement("$T $L = ".concat(getDateStringExpression), STRING, tmpVarName, key);
        cbBuilder.addStatement("$T $L = $T.getDateFromString($L)", paramType, variableName, DATE_UTILS, tmpVarName);
    }

    private static void writeDate(CodeBlock.Builder cbBuilder, String bundleVariableName, String variableAccessor, String key) {
        String tmpVarName = key.concat("DateString");
        cbBuilder.addStatement("$T $L = $T.getDateString($L)", STRING, tmpVarName, DATE_UTILS, variableAccessor);
        cbBuilder.addStatement("$L.putString($S,$L)", bundleVariableName, key, tmpVarName);
    }

    private static void readOffsetDate(CodeBlock.Builder cbBuilder, TypeName paramType, String bundleVariableName, String variableName, String key) {
        String tmpVarName = variableName.concat("DateString");
        String getDateStringExpression = String.format("%s.getString($S)", bundleVariableName);
        cbBuilder.addStatement("$T $L = ".concat(getDateStringExpression), STRING, tmpVarName, key);
        cbBuilder.addStatement("$T $L = $T.getOffsetDateFromString($L)", paramType, variableName, DATE_UTILS, tmpVarName);
    }

    private static void writeOffsetDate(CodeBlock.Builder cbBuilder, String bundleVariableName, String variableAccessor, String key) {
        String tmpVarName = key.concat("DateString");
        cbBuilder.addStatement("$T $L = $T.getOffsetDateString($L)", STRING, tmpVarName, DATE_UTILS, variableAccessor);
        cbBuilder.addStatement("$L.putString($S,$L)", bundleVariableName, key, tmpVarName);
    }

    private static void readParcelable(CodeBlock.Builder cbBuilder, TypeName paramType, String bundleVariableName, String variableName, String key) {
        cbBuilder.addStatement("$T $L", paramType, variableName)
                .beginControlFlow("if($L >= $L)", ANDROID_SDK_INT, generateVersionCodeClassName("TIRAMISU"))
                .addStatement("$L = $L.getParcelable($S,$L)", bundleVariableName, variableName, key, paramType.toString().concat(".class"))
                .nextControlFlow("else")
                .addStatement("$L = $L.getParcelable($S)", bundleVariableName, variableName, key)
                .endControlFlow();
    }

    private static void writeParcelable(CodeBlock.Builder cbBuilder, String bundleVariableName, String variableAccessor, String key) {
        cbBuilder.addStatement("$L.putParcelable($S,$L)", bundleVariableName, key, variableAccessor);
    }
}
