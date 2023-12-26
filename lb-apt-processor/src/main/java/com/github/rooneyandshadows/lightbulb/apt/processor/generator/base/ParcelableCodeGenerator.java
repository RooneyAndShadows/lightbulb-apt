package com.github.rooneyandshadows.lightbulb.apt.processor.generator.base;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.*;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.TypeUtils.*;

public class ParcelableCodeGenerator {

    public static void generateWriteStatement(TypeName paramType, CodeBlock.Builder cbBuilder, String bundleVariableName, String variableAccessor, String variableName) {
        String typeString = paramType.toString();

        if (isString(typeString)) writeString(cbBuilder, paramType, bundleVariableName, variableAccessor);
        else if (isUUID(typeString)) writeUUID(cbBuilder, paramType, bundleVariableName, variableAccessor);
        else if (isInt(typeString)) writeInt(cbBuilder, paramType, bundleVariableName, variableAccessor);
        else if (isBoolean(typeString)) writeBoolean(cbBuilder, paramType, bundleVariableName, variableAccessor);
        else if (isFloat(typeString)) writeFloat(cbBuilder, paramType, bundleVariableName, variableAccessor);
        else if (isLong(typeString)) writeLong(cbBuilder, paramType, bundleVariableName, variableAccessor);
        else if (isDouble(typeString)) writeDouble(cbBuilder, paramType, bundleVariableName, variableAccessor);
        else if (isDate(typeString)) writeDate(cbBuilder, paramType, bundleVariableName, variableAccessor);
        else if (isOffsetDate(typeString)) writeOffsetDate(cbBuilder, paramType, bundleVariableName, variableAccessor);
        else writeParcelable(cbBuilder, paramType, bundleVariableName, variableAccessor);
    }

    private static void writeOffsetDate(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableAccessor) {
        String tmpVarName = "offsetDateString";

        cbBuilder.addStatement("$T isNull = $L == null ? 0 else 1", TypeName.INT);
        cbBuilder.addStatement("$L.writeByte(($T)isNull))", parcelVariableName, TypeName.BYTE);
        cbBuilder.beginControlFlow("if($L != null)", variableAccessor)
                .addStatement("$T $L = $T.getOffsetDateString($L)", STRING, tmpVarName, DATE_UTILS, variableAccessor)
                .addStatement("$L.writeString($L)", parcelVariableName, tmpVarName)
                .endControlFlow();
    }

    private static void readOffsetDate(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableName) {
        String existenceVar = variableName.concat("Exists");
        String tmpVarName = variableName.concat("OffsetDateString");

        cbBuilder.addStatement("$T $L = null", varType, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", TypeName.BOOLEAN, existenceVar, TypeName.INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .addStatement("$T $L = $L.readString()", STRING, tmpVarName, parcelVariableName)
                .addStatement("$L = $T.getOffsetDateFromString($L)", variableName, DATE_UTILS, tmpVarName)
                .endControlFlow();
    }

    private static void writeDate(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableAccessor) {
        String tmpVarName = "dateString";

        cbBuilder.addStatement("$T isNull = $L == null ? 0 else 1", TypeName.INT);
        cbBuilder.addStatement("$L.writeByte(($T)isNull))", parcelVariableName, TypeName.BYTE);
        cbBuilder.beginControlFlow("if($L != null)", variableAccessor)
                .addStatement("$T $L = $T.getDateString($L)", STRING, tmpVarName, DATE_UTILS, variableAccessor)
                .addStatement("$L.writeString($L)", parcelVariableName, tmpVarName)
                .endControlFlow();
    }

    private static void readDate(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableName) {
        String existenceVar = variableName.concat("Exists");
        String tmpVarName = variableName.concat("DateString");

        cbBuilder.addStatement("$T $L = null", varType, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", TypeName.BOOLEAN, existenceVar, TypeName.INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .addStatement("$T $L = $L.readString()", STRING, tmpVarName, parcelVariableName)
                .addStatement("$L = $T.getDateFromString($L)", variableName, DATE_UTILS, tmpVarName)
                .endControlFlow();
    }

    private static void writeInt(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableAccessor) {
        boolean isPrimitive = varType.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$L.writeInt($L)", parcelVariableName, variableAccessor);
        } else {
            cbBuilder.addStatement("$T isNull = $L == null ? 0 else 1", TypeName.INT);
            cbBuilder.addStatement("$L.writeByte(($T)isNull))", parcelVariableName, TypeName.BYTE);
            cbBuilder.beginControlFlow("if($L != null)", variableAccessor)
                    .addStatement("$L.writeInt($L)", parcelVariableName, variableAccessor)
                    .endControlFlow();
        }
    }

    private static void readInt(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableName) {
        boolean isPrimitive = varType.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$T $L = $L.readInt()", varType, variableName, parcelVariableName);
        } else {
            String existenceVar = variableName.concat("Exists");
            cbBuilder.addStatement("$T $L = null", varType, variableName);
            cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", TypeName.BOOLEAN, existenceVar, TypeName.INT, parcelVariableName);
            cbBuilder.beginControlFlow("if($L)", existenceVar)
                    .addStatement("$L = $L.readInt()", variableName, parcelVariableName)
                    .endControlFlow();
        }
    }

    private static void writeString(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableAccessor) {
        cbBuilder.addStatement("$T isNull = $L == null ? 0 else 1", TypeName.INT);
        cbBuilder.addStatement("$L.writeByte(($T)isNull))", parcelVariableName, TypeName.BYTE);
        cbBuilder.beginControlFlow("if($L != null)", variableAccessor)
                .addStatement("$L.writeString($L)", parcelVariableName, variableAccessor)
                .endControlFlow();
    }

    private static void readString(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableName) {
        String existenceVar = variableName.concat("Exists");
        cbBuilder.addStatement("$T $L = null", varType, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", TypeName.BOOLEAN, existenceVar, TypeName.INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .addStatement("$L = $L.readInt()", varType, variableName, parcelVariableName)
                .endControlFlow();
    }

    private static void writeDouble(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableAccessor) {
        boolean isPrimitive = varType.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$L.writeDouble($L)", parcelVariableName, variableAccessor);
        } else {
            cbBuilder.addStatement("$T isNull = $L == null ? 0 else 1", TypeName.INT);
            cbBuilder.addStatement("$L.writeByte(($T)isNull))", parcelVariableName, TypeName.BYTE);
            cbBuilder.beginControlFlow("if($L != null)", variableAccessor)
                    .addStatement("$L.writeDouble($L)", parcelVariableName, variableAccessor)
                    .endControlFlow();
        }
    }

    private static void readDouble(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableName) {
        boolean isPrimitive = varType.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$T $L = $L.readDouble()", varType, variableName, parcelVariableName);
        } else {
            String existenceVar = variableName.concat("Exists");
            cbBuilder.addStatement("$T $L = null", varType, variableName);
            cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", TypeName.BOOLEAN, existenceVar, TypeName.INT, parcelVariableName);
            cbBuilder.beginControlFlow("if($L)", existenceVar)
                    .addStatement("$L = $L.readDouble()", varType, variableName, parcelVariableName)
                    .endControlFlow();
        }
    }

    private static void writeFloat(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableAccessor) {
        boolean isPrimitive = varType.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$L.writeFloat($L)", parcelVariableName, variableAccessor);
        } else {
            cbBuilder.addStatement("$T isNull = $L == null ? 0 else 1", TypeName.INT);
            cbBuilder.addStatement("$L.writeByte(($T)isNull))", parcelVariableName, TypeName.BYTE);
            cbBuilder.beginControlFlow("if($L != null)", variableAccessor)
                    .addStatement("$L.writeFloat($L)", parcelVariableName, variableAccessor)
                    .endControlFlow();
        }
    }

    private static void readFloat(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableName) {
        boolean isPrimitive = varType.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$T $L = $L.readFloat()", varType, variableName, parcelVariableName);
        } else {
            String existenceVar = variableName.concat("Exists");
            cbBuilder.addStatement("$T $L = null", varType, variableName);
            cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", TypeName.BOOLEAN, existenceVar, TypeName.INT, parcelVariableName);
            cbBuilder.beginControlFlow("if($L)", existenceVar)
                    .addStatement("$L = $L.readFloat()", varType, variableName, parcelVariableName)
                    .endControlFlow();
        }
    }

    private static void writeLong(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableAccessor) {
        boolean isPrimitive = varType.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$L.writeLong($L)", parcelVariableName, variableAccessor);
        } else {
            cbBuilder.addStatement("$T isNull = $L == null ? 0 else 1", TypeName.INT);
            cbBuilder.addStatement("$L.writeByte(($T)isNull))", parcelVariableName, TypeName.BYTE);
            cbBuilder.beginControlFlow("if($L != null)", variableAccessor)
                    .addStatement("$L.writeLong($L)", parcelVariableName, variableAccessor)
                    .endControlFlow();
        }
    }

    private static void readLong(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableName) {
        boolean isPrimitive = varType.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$T $L = $L.readLong()", varType, variableName, parcelVariableName);
        } else {
            String existenceVar = variableName.concat("Exists");
            cbBuilder.addStatement("$T $L = null", varType, variableName);
            cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", TypeName.BOOLEAN, existenceVar, TypeName.INT, parcelVariableName);
            cbBuilder.beginControlFlow("if($L)", existenceVar)
                    .addStatement("$L = $L.readLong()", varType, variableName, parcelVariableName)
                    .endControlFlow();
        }
    }

    private static void writeBoolean(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableAccessor) {
        boolean isPrimitive = varType.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$L.writeInt($L ? 1 : 0)", parcelVariableName, variableAccessor);
        } else {
            cbBuilder.addStatement("$T isNull = $L == null ? 0 else 1", TypeName.INT);
            cbBuilder.addStatement("$L.writeByte(($T)isNull))", parcelVariableName, TypeName.BYTE);
            cbBuilder.beginControlFlow("if($L != null)", variableAccessor)
                    .addStatement("$L.writeInt($L ? 1 : 0)", parcelVariableName, variableAccessor)
                    .endControlFlow();
        }
    }

    private static void readBoolean(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableName) {
        boolean isPrimitive = varType.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$T $L = $L.readInt() == 1", varType, variableName, parcelVariableName);
        } else {
            String existenceVar = variableName.concat("Exists");
            cbBuilder.addStatement("$T $L = null", varType, variableName);
            cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", TypeName.BOOLEAN, existenceVar, TypeName.INT, parcelVariableName);
            cbBuilder.beginControlFlow("if($L)", existenceVar)
                    .addStatement("$L = $L.readInt() == 1", varType, variableName, parcelVariableName)
                    .endControlFlow();
        }
    }

    private static void writeUUID(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableAccessor) {
        String tmpVarName = "uuidString";

        cbBuilder.addStatement("$T isNull = $L == null ? 0 else 1", TypeName.INT);
        cbBuilder.addStatement("$L.writeByte(($T)isNull))", parcelVariableName, TypeName.BYTE);
        cbBuilder.beginControlFlow("if($L != null)", variableAccessor)
                .addStatement("$T $L = $L.toString()", STRING, tmpVarName, variableAccessor)
                .addStatement("$L.writeString($L)", parcelVariableName, tmpVarName)
                .endControlFlow();
    }

    private static void readUUID(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableName) {
        String existenceVar = variableName.concat("Exists");
        String tmpVarName = variableName.concat("UuidString");

        cbBuilder.addStatement("$T $L = null", varType, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", TypeName.BOOLEAN, existenceVar, TypeName.INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .addStatement("$T $L = $L.readString()", STRING, tmpVarName, parcelVariableName)
                .addStatement("$L = $T.fromString($L)", variableName, UUID, tmpVarName)
                .endControlFlow();
    }

    private static void writeParcelable(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableAccessor) {
        cbBuilder.addStatement("$T isNull = $L == null ? 0 else 1", TypeName.INT);
        cbBuilder.addStatement("$L.writeByte(($T)isNull))", parcelVariableName, TypeName.BYTE);
        cbBuilder.beginControlFlow("if($L != null)", variableAccessor)
                .addStatement("$L.writeParcelable($L,0)", parcelVariableName, variableAccessor)
                .endControlFlow();
    }

    private static void readParcelable(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableName) {
        String existenceVar = variableName.concat("Exists");

        cbBuilder.addStatement("$T $L = null", varType, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", TypeName.BOOLEAN, existenceVar, TypeName.INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .beginControlFlow("if($L >= $L)", ANDROID_SDK_INT, generateVersionCodeClassName("TIRAMISU"))
                .addStatement("$L = $L.readParcelable(this.getClass().getClassLoader(),$T.class)", variableName, parcelVariableName, varType)
                .nextControlFlow("else")
                .addStatement("$L = $L.getParcelable(this.getClass().getClassLoader())", variableName, parcelVariableName)
                .endControlFlow()
                .endControlFlow();
    }

    private static void writeSparseArray(CodeBlock.Builder cbBuilder, String parcelVariableName, String variableAccessor) {
        cbBuilder.addStatement("$T isNull = $L == null ? 0 else 1", TypeName.INT);
        cbBuilder.addStatement("$L.writeByte(($T)isNull))", parcelVariableName, TypeName.BYTE);
        cbBuilder.beginControlFlow("if($L != null)", variableAccessor)
                .addStatement("$L.writeSparseArray($L)", parcelVariableName, variableAccessor)
                .endControlFlow();
    }

    private static void readSparseArray(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableName) {
        String existenceVar = variableName.concat("Exists");

        cbBuilder.addStatement("$T $L = null", varType, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", TypeName.BOOLEAN, existenceVar, TypeName.INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .beginControlFlow("if($L >= $L)", ANDROID_SDK_INT, generateVersionCodeClassName("TIRAMISU"))
                .addStatement("$L = $L.readSparseArray(this.getClass().getClassLoader(),$T.class)", variableName, parcelVariableName, varType)
                .nextControlFlow("else")
                .addStatement("$L = $L.readSparseArray(this.getClass().getClassLoader())", variableName, parcelVariableName)
                .endControlFlow()
                .endControlFlow();
    }

    private static void writeList(CodeBlock.Builder cbBuilder, String parcelVariableName, String variableAccessor) {
        cbBuilder.addStatement("$T isNull = $L == null ? 0 else 1", TypeName.INT);
        cbBuilder.addStatement("$L.writeByte(($T)isNull))", parcelVariableName, TypeName.BYTE);
        cbBuilder.beginControlFlow("if($L != null)", variableAccessor)
                .addStatement("$L.writeList($L)", parcelVariableName, variableAccessor)
                .endControlFlow();
    }

    private static void readList(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableName) {
        String existenceVar = variableName.concat("Exists");

        cbBuilder.addStatement("$T $L = null", varType, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", TypeName.BOOLEAN, existenceVar, TypeName.INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .beginControlFlow("if($L >= $L)", ANDROID_SDK_INT, generateVersionCodeClassName("TIRAMISU"))
                .addStatement("$L = $L.readList(this.getClass().getClassLoader(),$T.class)", variableName, parcelVariableName, varType)
                .nextControlFlow("else")
                .addStatement("$L = $L.readList(this.getClass().getClassLoader())", variableName, parcelVariableName)
                .endControlFlow()
                .endControlFlow();
    }

    private static void writeMap(CodeBlock.Builder cbBuilder, String parcelVariableName, String variableAccessor) {
        cbBuilder.addStatement("$T isNull = $L == null ? 0 else 1", TypeName.INT);
        cbBuilder.addStatement("$L.writeByte(($T)isNull))", parcelVariableName, TypeName.BYTE);
        cbBuilder.beginControlFlow("if($L != null)", variableAccessor)
                .addStatement("$L.writeMap($L)", parcelVariableName, variableAccessor)
                .endControlFlow();
    }

    private static void readMap(CodeBlock.Builder cbBuilder, TypeName varType, String parcelVariableName, String variableName) {
        String existenceVar = variableName.concat("Exists");

        cbBuilder.addStatement("$T $L = null", varType, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", TypeName.BOOLEAN, existenceVar, TypeName.INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .beginControlFlow("if($L >= $L)", ANDROID_SDK_INT, generateVersionCodeClassName("TIRAMISU"))
                .addStatement("$L = $L.readMap(this.getClass().getClassLoader(),$T.class)", variableName, parcelVariableName, varType)
                .nextControlFlow("else")
                .addStatement("$L = $L.readMap(this.getClass().getClassLoader())", variableName, parcelVariableName)
                .endControlFlow()
                .endControlFlow();
    }

    //TypeName TYPE_LIST = ClassName.get(List.class);
   // boolean isList = isFromType(type, TYPE_LIST)


}
