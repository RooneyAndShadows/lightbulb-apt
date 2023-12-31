package com.github.rooneyandshadows.lightbulb.apt.processor.utils;

import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.base.DeclaredValueHolder;
import com.github.rooneyandshadows.lightbulb.apt.processor.TypeInformation;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.*;

public class BundleCodeGenerator {

    public static CodeBlock generateWriteStatement(
            DeclaredValueHolder valueHolder,
            String bundleVariableName
    ) {
        TypeInformation type = valueHolder.getTypeInformation();
        String key = String.format("KEY_%s", valueHolder.getName());
        boolean checkForNull = !type.isPrimitive();

        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

        if (checkForNull) {
            codeBlockBuilder.beginControlFlow("if($L != null)", valueHolder.getValueAccessor());
        }

        if (type.isString()) {
            writeString(codeBlockBuilder, bundleVariableName, valueHolder, key);
        } else if (type.isUUID()) {
            writeUUID(codeBlockBuilder, bundleVariableName, valueHolder, key);
        } else if (type.isInt()) {
            writeInt(codeBlockBuilder, bundleVariableName, valueHolder, key);
        } else if (type.isBoolean()) {
            writeBoolean(codeBlockBuilder, bundleVariableName, valueHolder, key);
        } else if (type.isFloat()) {
            writeFloat(codeBlockBuilder, bundleVariableName, valueHolder, key);
        } else if (type.isLong()) {
            writeLong(codeBlockBuilder, bundleVariableName, valueHolder, key);
        } else if (type.isDouble()) {
            writeDouble(codeBlockBuilder, bundleVariableName, valueHolder, key);
        } else if (type.isDate()) {
            writeDate(codeBlockBuilder, bundleVariableName, valueHolder, key);
        } else if (type.isOffsetDate()) {
            writeOffsetDate(codeBlockBuilder, bundleVariableName, valueHolder, key);
        } else if (type.is(ANDROID_PARCELABLE)) {
            writeParcelable(codeBlockBuilder, bundleVariableName, valueHolder, key);
        } else {
            //NOT SUPPORTED TYPE
        }

        if (checkForNull) {
            codeBlockBuilder.endControlFlow();
        }

        return codeBlockBuilder.build();
    }

    public static CodeBlock generateReadStatement(
            DeclaredValueHolder valueHolder,
            String bundleVariableName,
            boolean requireExistence,
            boolean requireNotNull
    ) {
        TypeInformation type = valueHolder.getTypeInformation();
        String key = String.format("KEY_%s", valueHolder.getName());
        String tmpVarName = valueHolder.getName().concat("FromBundle");

        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

        codeBlockBuilder.beginControlFlow("if($L.containsKey($S))", bundleVariableName, key);

        if (type.isString()) {
            readString(codeBlockBuilder, bundleVariableName, valueHolder, key, tmpVarName);
        } else if (type.isUUID()) {
            readUUID(codeBlockBuilder, bundleVariableName, valueHolder, key, tmpVarName);
        } else if (type.isInt()) {
            readInt(codeBlockBuilder, bundleVariableName, valueHolder, key, tmpVarName);
        } else if (type.isBoolean()) {
            readBoolean(codeBlockBuilder, bundleVariableName, valueHolder, key, tmpVarName);
        } else if (type.isFloat()) {
            readFloat(codeBlockBuilder, bundleVariableName, valueHolder, key, tmpVarName);
        } else if (type.isLong()) {
            readLong(codeBlockBuilder, bundleVariableName, valueHolder, key, tmpVarName);
        } else if (type.isDouble()) {
            readDouble(codeBlockBuilder, bundleVariableName, valueHolder, key, tmpVarName);
        } else if (type.isDate()) {
            readDate(codeBlockBuilder, bundleVariableName, valueHolder, key, tmpVarName);
        } else if (type.isOffsetDate()) {
            readOffsetDate(codeBlockBuilder, bundleVariableName, valueHolder, key, tmpVarName);
        } else if (type.is(ANDROID_PARCELABLE)) {
            readParcelable(codeBlockBuilder, bundleVariableName, valueHolder, key, tmpVarName);
        } else {
            //NOT SUPPORTED TYPE
        }

        if (requireNotNull && !type.isPrimitive()) {
            String errorMessage = String.format("%s can not be null but null value received from bundle.", valueHolder.getName());
            codeBlockBuilder.beginControlFlow("if($L == null)", valueHolder.getValueAccessor())
                    .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, errorMessage)
                    .endControlFlow();
        }

        if (requireExistence) {
            String errorMessage = String.format("Value for key %s is required but could not be found in bundle", key);
            codeBlockBuilder.nextControlFlow("else")
                    .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, errorMessage)
                    .endControlFlow();
        } else {
            codeBlockBuilder.endControlFlow();
        }

        return codeBlockBuilder.build();
    }

    public static CodeBlock generateReadStatement(
            DeclaredValueHolder valueHolder,
            String bundleVariableName
    ) {
        return generateReadStatement(valueHolder, bundleVariableName, false, false);
    }

    private static void readString(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key,
            String tmpVarName
    ) {
        TypeInformation typeInformation = valueHolder.getTypeInformation();
        TypeName typeName = TypeName.get(typeInformation.getTypeMirror());
        String setStatement = valueHolder.getValueSetStatement(tmpVarName);

        cbBuilder.addStatement("$T $L = $L.getString($S,\"\")", typeName, tmpVarName, bundleVariableName, key)
                 .addStatement(setStatement);
    }

    private static void writeString(CodeBlock.Builder cbBuilder, String bundleVariableName, DeclaredValueHolder valueHolder, String key) {
        cbBuilder.addStatement("$L.putString($S,$L)", bundleVariableName, key, valueHolder.getValueAccessor());
    }

    private static void readUUID(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key,
            String tmpVarName
    ) {
        TypeInformation typeInformation = valueHolder.getTypeInformation();
        TypeName typeName = TypeName.get(typeInformation.getTypeMirror());
        String setStatement = valueHolder.getValueSetStatement(tmpVarName);

        cbBuilder.addStatement("$T $L = $T.fromString($L.getString($S,\"\"))", typeName, tmpVarName, typeName, bundleVariableName, key)
                 .addStatement(setStatement);
    }

    private static void writeUUID(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key
    ) {
        cbBuilder.addStatement("$L.putString($S,$L.toString())", bundleVariableName, key, valueHolder.getValueAccessor());
    }

    private static void readInt(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key,
            String tmpVarName
    ) {
        TypeInformation typeInformation = valueHolder.getTypeInformation();
        TypeName typeName = TypeName.get(typeInformation.getTypeMirror());
        String setStatement = valueHolder.getValueSetStatement(tmpVarName);

        cbBuilder.addStatement("$T $L = $L.getInt($S)", typeName, tmpVarName, bundleVariableName, key)
                .addStatement(setStatement);
    }

    private static void writeInt(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key
    ) {
        cbBuilder.addStatement("$L.putInt($S,$L)", bundleVariableName, key, valueHolder.getValueAccessor());
    }

    private static void readBoolean(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key,
            String tmpVarName
    ) {
        TypeInformation typeInformation = valueHolder.getTypeInformation();
        TypeName typeName = TypeName.get(typeInformation.getTypeMirror());
        String setStatement = valueHolder.getValueSetStatement(tmpVarName);

        cbBuilder.addStatement("$T $L = $L.getBoolean($S)", typeName, tmpVarName, bundleVariableName, key)
                 .addStatement(setStatement);
    }

    private static void writeBoolean(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key
    ) {
        cbBuilder.addStatement("$L.putBoolean($S,$L)", bundleVariableName, key, valueHolder.getValueAccessor());
    }

    private static void readFloat(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key,
            String tmpVarName
    ) {
        TypeInformation typeInformation = valueHolder.getTypeInformation();
        TypeName typeName = TypeName.get(typeInformation.getTypeMirror());
        String setStatement = valueHolder.getValueSetStatement(tmpVarName);

        cbBuilder.addStatement("$T $L = $L.getFloat($S)", typeName, tmpVarName, bundleVariableName, key)
                 .addStatement(setStatement);
    }

    private static void writeFloat(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key
    ) {
        cbBuilder.addStatement("$L.putFloat($S,$L)", bundleVariableName, key, valueHolder.getValueAccessor());
    }

    private static void readLong(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key,
            String tmpVarName
    ) {
        TypeInformation typeInformation = valueHolder.getTypeInformation();
        TypeName typeName = TypeName.get(typeInformation.getTypeMirror());
        String setStatement = valueHolder.getValueSetStatement(tmpVarName);

        cbBuilder.addStatement("$T $L = $L.getLong($S)", typeName, tmpVarName, bundleVariableName, key)
                 .addStatement(setStatement);
    }

    private static void writeLong(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key
    ) {
        cbBuilder.addStatement("$L.putLong($S,$L)", bundleVariableName, key, valueHolder.getValueAccessor());
    }

    private static void readDouble(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key,
            String tmpVarName
    ) {
        TypeInformation typeInformation = valueHolder.getTypeInformation();
        TypeName typeName = TypeName.get(typeInformation.getTypeMirror());
        String setStatement = valueHolder.getValueSetStatement(tmpVarName);

        cbBuilder.addStatement("$T $L = $L.getDouble($S)", typeName, tmpVarName, bundleVariableName, key)
                 .addStatement(setStatement);
    }

    private static void writeDouble(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key
    ) {
        cbBuilder.addStatement("$L.putDouble($S,$L)", bundleVariableName, key, valueHolder.getValueAccessor());
    }

    private static void readDate(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key,
            String tmpVarName
    ) {
        TypeInformation typeInformation = valueHolder.getTypeInformation();
        TypeName typeName = TypeName.get(typeInformation.getTypeMirror());
        String setStatement = valueHolder.getValueSetStatement(tmpVarName);
        String dateStringTmpVarName = key.concat("DateString");

        String getDateStringExpression = String.format("%s.getString($S)", bundleVariableName);
        cbBuilder.addStatement("$T $L = ".concat(getDateStringExpression), STRING, dateStringTmpVarName, key);
        cbBuilder.addStatement("$T $L = $T.getDateFromString($L)", typeName, tmpVarName, DATE_UTILS, dateStringTmpVarName)
                 .addStatement(setStatement);
    }

    private static void writeDate(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key
    ) {
        String tmpVarName = valueHolder.getName().concat("DateString");

        cbBuilder.addStatement("$T $L = $T.getDateString($L)", STRING, tmpVarName, DATE_UTILS, valueHolder.getValueAccessor())
                .addStatement("$L.putString($S,$L)", bundleVariableName, key, tmpVarName);
    }

    private static void readOffsetDate(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key,
            String tmpVarName
    ) {
        TypeInformation typeInformation = valueHolder.getTypeInformation();
        TypeName typeName = TypeName.get(typeInformation.getTypeMirror());
        String setStatement = valueHolder.getValueSetStatement(tmpVarName);
        String dateStringTmpVarName = key.concat("OffsetDateString");

        String getDateStringExpression = String.format("%s.getString($S)", bundleVariableName);
        cbBuilder.addStatement("$T $L = ".concat(getDateStringExpression), STRING, dateStringTmpVarName, key);
        cbBuilder.addStatement("$T $L = $T.getOffsetDateFromString($L)", typeName, tmpVarName, DATE_UTILS, dateStringTmpVarName)
                 .addStatement(setStatement);
    }

    private static void writeOffsetDate(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key
    ) {
        String tmpVarName = valueHolder.getName().concat("OffsetDateString");

        cbBuilder.addStatement("$T $L = $T.getOffsetDateString($L)", STRING, tmpVarName, DATE_UTILS, valueHolder.getValueAccessor())
                .addStatement("$L.putString($S,$L)", bundleVariableName, key, tmpVarName);
    }

    private static void readParcelable(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key,
            String tmpVarName
    ) {
        TypeInformation typeInformation = valueHolder.getTypeInformation();
        TypeName typeName = TypeName.get(typeInformation.getTypeMirror());
        String setStatement = valueHolder.getValueSetStatement(tmpVarName);

        cbBuilder.addStatement("$T $L", typeName, tmpVarName)
                .beginControlFlow("if($L >= $L)", ANDROID_SDK_INT, generateVersionCodeClassName("TIRAMISU"))
                .addStatement("$L = $L.getParcelable($S,$L)", bundleVariableName, tmpVarName, key, typeName.toString().concat(".class"))
                .nextControlFlow("else")
                .addStatement("$L = $L.getParcelable($S)", bundleVariableName, tmpVarName, key)
                .endControlFlow()
                .addStatement(setStatement);
    }

    private static void writeParcelable(
            CodeBlock.Builder cbBuilder,
            String bundleVariableName,
            DeclaredValueHolder valueHolder,
            String key
    ) {
        cbBuilder.addStatement("$L.putParcelable($S,$L)", bundleVariableName, key, valueHolder.getValueAccessor());
    }
}
