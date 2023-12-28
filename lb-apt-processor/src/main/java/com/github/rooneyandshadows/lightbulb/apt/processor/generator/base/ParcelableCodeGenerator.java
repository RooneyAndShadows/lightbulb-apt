package com.github.rooneyandshadows.lightbulb.apt.processor.generator.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.base.TypeInformation;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.*;
import static com.squareup.javapoet.TypeName.*;

@SuppressWarnings("DuplicatedCode")
public class ParcelableCodeGenerator {

    public static void generateWriteStatement(CodeBlock.Builder cbBuilder, Field field, String bundleVariableName) {
        TypeInformation type = field.getTypeInformation();

        if (type.isString()) {
            writeString(cbBuilder, field, bundleVariableName);
        } else if (type.isUUID()) {
            writeUUID(cbBuilder, field, bundleVariableName);
        } else if (type.isInt()) {
            writeInt(cbBuilder, field, bundleVariableName);
        } else if (type.isBoolean()) {
            writeBoolean(cbBuilder, field, bundleVariableName);
        } else if (type.isFloat()) {
            writeFloat(cbBuilder, field, bundleVariableName);
        } else if (type.isLong()) {
            writeLong(cbBuilder, field, bundleVariableName);
        } else if (type.isDouble()) {
            writeDouble(cbBuilder, field, bundleVariableName);
        } else if (type.isDate()) {
            writeDate(cbBuilder, field, bundleVariableName);
        } else if (type.isOffsetDate()) {
            writeOffsetDate(cbBuilder, field, bundleVariableName);
        } else if (type.is(ANDROID_PARCELABLE)) {
            writeParcelable(cbBuilder, field, bundleVariableName);
        } else if (type.is(ANDROID_SPARSE_ARRAY)) {
            writeSparseArray(cbBuilder, field, bundleVariableName);
        } else if (type.isList()) {
            writeList(cbBuilder, field, bundleVariableName);
        } else if (type.isMap()) {
            writeMap(cbBuilder, field, bundleVariableName);
        }
    }

    public static void generateReadStatement(CodeBlock.Builder cbBuilder, Field field, String bundleVariableName) {
        TypeInformation type = field.getTypeInformation();

        if (type.isString()) {
            readString(cbBuilder, field, bundleVariableName);
        } else if (type.isUUID()) {
            readUUID(cbBuilder, field, bundleVariableName);
        } else if (type.isInt()) {
            readInt(cbBuilder, field, bundleVariableName);
        } else if (type.isBoolean()) {
            readBoolean(cbBuilder, field, bundleVariableName);
        } else if (type.isFloat()) {
            readFloat(cbBuilder, field, bundleVariableName);
        } else if (type.isLong()) {
            readLong(cbBuilder, field, bundleVariableName);
        } else if (type.isDouble()) {
            readDouble(cbBuilder, field, bundleVariableName);
        } else if (type.isDate()) {
            readDate(cbBuilder, field, bundleVariableName);
        } else if (type.isOffsetDate()) {
            readOffsetDate(cbBuilder, field, bundleVariableName);
        } else if (type.is(ANDROID_PARCELABLE)) {
            readParcelable(cbBuilder, field, bundleVariableName);
        } else if (type.is(ANDROID_SPARSE_ARRAY)) {
            readSparseArray(cbBuilder, field, bundleVariableName);
        } else if (type.isList()) {
            readList(cbBuilder, field, bundleVariableName);
        } else if (type.isMap()) {
            readMap(cbBuilder, field, bundleVariableName);
        } else {
            //NOT SUPPORTED TYPE
        }
    }

    private static void writeOffsetDate(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        String variableName = field.getName();
        String nullMarkerVariable = variableName.concat("Marker");
        String tmpVarName = variableName.concat("OffsetDateString");
        String fieldAccessor = String.format("this.%s", field.getName());

        cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, fieldAccessor);
        cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
        cbBuilder.beginControlFlow("if($L != null)", fieldAccessor)
                .addStatement("$T $L = $T.getOffsetDateString($L)", STRING, tmpVarName, DATE_UTILS, fieldAccessor)
                .addStatement("$L.writeString($L)", parcelVariableName, tmpVarName)
                .endControlFlow();
    }

    private static void readOffsetDate(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeName typeName = field.getTypeInformation().getTypeName();
        String variableName = field.getName();
        String existenceVar = variableName.concat("Exists");
        String tmpVarName = variableName.concat("OffsetDateString");

        cbBuilder.addStatement("$T $L = null", typeName, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .addStatement("$T $L = $L.readString()", STRING, tmpVarName, parcelVariableName)
                .addStatement("$L = $T.getOffsetDateFromString($L)", variableName, DATE_UTILS, tmpVarName)
                .endControlFlow();
    }

    private static void writeDate(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        String tmpVarName = "dateString";
        String variableName = field.getName();
        String nullMarkerVariable = variableName.concat("Marker");
        String fieldAccessor = String.format("this.%s", field.getName());

        cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, fieldAccessor);
        cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
        cbBuilder.beginControlFlow("if($L != null)", fieldAccessor)
                .addStatement("$T $L = $T.getDateString($L)", STRING, tmpVarName, DATE_UTILS, fieldAccessor)
                .addStatement("$L.writeString($L)", parcelVariableName, tmpVarName)
                .endControlFlow();
    }

    private static void readDate(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeName typeName = field.getTypeInformation().getTypeName();
        String variableName = field.getName();
        String existenceVar = variableName.concat("Exists");
        String tmpVarName = variableName.concat("DateString");

        cbBuilder.addStatement("$T $L = null", typeName, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .addStatement("$T $L = $L.readString()", STRING, tmpVarName, parcelVariableName)
                .addStatement("$L = $T.getDateFromString($L)", variableName, DATE_UTILS, tmpVarName)
                .endControlFlow();
    }

    private static void writeInt(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeName typeName = field.getTypeInformation().getTypeName();
        String fieldAccessor = String.format("this.%s", field.getName());
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$L.writeInt($L)", parcelVariableName, fieldAccessor);
        } else {
            String variableName = field.getName();
            String nullMarkerVariable = variableName.concat("Marker");

            cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, fieldAccessor);
            cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
            cbBuilder.beginControlFlow("if($L != null)", fieldAccessor)
                    .addStatement("$L.writeInt($L)", parcelVariableName, fieldAccessor)
                    .endControlFlow();
        }
    }

    private static void readInt(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeName typeName = field.getTypeInformation().getTypeName();
        String variableName = field.getName();
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$T $L = $L.readInt()", typeName, variableName, parcelVariableName);
        } else {
            String existenceVar = variableName.concat("Exists");
            cbBuilder.addStatement("$T $L = null", typeName, variableName);
            cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
            cbBuilder.beginControlFlow("if($L)", existenceVar)
                    .addStatement("$L = $L.readInt()", variableName, parcelVariableName)
                    .endControlFlow();
        }
    }

    private static void writeString(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        String variableName = field.getName();
        String nullMarkerVariable = variableName.concat("Marker");
        String fieldAccessor = String.format("this.%s", field.getName());

        cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, fieldAccessor);
        cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
        cbBuilder.beginControlFlow("if($L != null)", fieldAccessor)
                .addStatement("$L.writeString($L)", parcelVariableName, fieldAccessor)
                .endControlFlow();
    }

    private static void readString(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeName typeName = field.getTypeInformation().getTypeName();
        String variableName = field.getName();
        String existenceVar = variableName.concat("Exists");

        cbBuilder.addStatement("$T $L = null", typeName, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .addStatement("$L = $L.readString()", variableName, parcelVariableName)
                .endControlFlow();
    }

    private static void writeDouble(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeName typeName = field.getTypeInformation().getTypeName();
        String fieldAccessor = String.format("this.%s", field.getName());
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$L.writeDouble($L)", parcelVariableName, fieldAccessor);
        } else {
            String variableName = field.getName();
            String nullMarkerVariable = variableName.concat("Marker");

            cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, fieldAccessor);
            cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
            cbBuilder.beginControlFlow("if($L != null)", fieldAccessor)
                    .addStatement("$L.writeDouble($L)", parcelVariableName, fieldAccessor)
                    .endControlFlow();
        }
    }

    private static void readDouble(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeName typeName = field.getTypeInformation().getTypeName();
        String variableName = field.getName();
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$T $L = $L.readDouble()", typeName, variableName, parcelVariableName);
        } else {
            String existenceVar = variableName.concat("Exists");
            cbBuilder.addStatement("$T $L = null", typeName, variableName);
            cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
            cbBuilder.beginControlFlow("if($L)", existenceVar)
                    .addStatement("$L = $L.readDouble()", variableName, parcelVariableName)
                    .endControlFlow();
        }
    }

    private static void writeFloat(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeName typeName = field.getTypeInformation().getTypeName();
        String fieldAccessor = String.format("this.%s", field.getName());
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$L.writeFloat($L)", parcelVariableName, fieldAccessor);
        } else {
            String variableName = field.getName();
            String nullMarkerVariable = variableName.concat("Marker");

            cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, fieldAccessor);
            cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
            cbBuilder.beginControlFlow("if($L != null)", fieldAccessor)
                    .addStatement("$L.writeFloat($L)", parcelVariableName, fieldAccessor)
                    .endControlFlow();
        }
    }

    private static void readFloat(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeName typeName = field.getTypeInformation().getTypeName();
        String variableName = field.getName();
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$T $L = $L.readFloat()", typeName, variableName, parcelVariableName);
        } else {
            String existenceVar = variableName.concat("Exists");
            cbBuilder.addStatement("$T $L = null", typeName, variableName);
            cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
            cbBuilder.beginControlFlow("if($L)", existenceVar)
                    .addStatement("$L = $L.readFloat()", variableName, parcelVariableName)
                    .endControlFlow();
        }
    }

    private static void writeLong(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeName typeName = field.getTypeInformation().getTypeName();
        String fieldAccessor = String.format("this.%s", field.getName());
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$L.writeLong($L)", parcelVariableName, fieldAccessor);
        } else {
            String variableName = field.getName();
            String nullMarkerVariable = variableName.concat("Marker");

            cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, fieldAccessor);
            cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
            cbBuilder.beginControlFlow("if($L != null)", fieldAccessor)
                    .addStatement("$L.writeLong($L)", parcelVariableName, fieldAccessor)
                    .endControlFlow();
        }
    }

    private static void readLong(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeName typeName = field.getTypeInformation().getTypeName();
        String variableName = field.getName();
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$T $L = $L.readLong()", typeName, variableName, parcelVariableName);
        } else {
            String existenceVar = variableName.concat("Exists");
            cbBuilder.addStatement("$T $L = null", typeName, variableName);
            cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
            cbBuilder.beginControlFlow("if($L)", existenceVar)
                    .addStatement("$L = $L.readLong()", variableName, parcelVariableName)
                    .endControlFlow();
        }
    }

    private static void writeBoolean(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeName typeName = field.getTypeInformation().getTypeName();
        String fieldAccessor = String.format("this.%s", field.getName());
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$L.writeInt($L ? 1 : 0)", parcelVariableName, fieldAccessor);
        } else {
            String variableName = field.getName();
            String nullMarkerVariable = variableName.concat("Marker");

            cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, fieldAccessor);
            cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
            cbBuilder.beginControlFlow("if($L != null)", fieldAccessor)
                    .addStatement("$L.writeInt($L ? 1 : 0)", parcelVariableName, fieldAccessor)
                    .endControlFlow();
        }
    }

    private static void readBoolean(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeName typeName = field.getTypeInformation().getTypeName();
        String variableName = field.getName();
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$T $L = $L.readInt() == 1", typeName, variableName, parcelVariableName);
        } else {
            String existenceVar = variableName.concat("Exists");
            cbBuilder.addStatement("$T $L = null", typeName, variableName);
            cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
            cbBuilder.beginControlFlow("if($L)", existenceVar)
                    .addStatement("$L = $L.readInt() == 1", variableName, parcelVariableName)
                    .endControlFlow();
        }
    }

    private static void writeUUID(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        String variableName = field.getName();
        String nullMarkerVariable = variableName.concat("Marker");
        String tmpVarName = variableName.concat("uuidString");
        String fieldAccessor = String.format("this.%s", field.getName());

        cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, fieldAccessor);
        cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
        cbBuilder.beginControlFlow("if($L != null)", fieldAccessor)
                .addStatement("$T $L = $L.toString()", STRING, tmpVarName, fieldAccessor)
                .addStatement("$L.writeString($L)", parcelVariableName, tmpVarName)
                .endControlFlow();
    }

    private static void readUUID(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeName typeName = field.getTypeInformation().getTypeName();
        String variableName = field.getName();
        String existenceVar = variableName.concat("Exists");
        String tmpVarName = variableName.concat("UuidString");

        cbBuilder.addStatement("$T $L = null", typeName, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .addStatement("$T $L = $L.readString()", STRING, tmpVarName, parcelVariableName)
                .addStatement("$L = $T.fromString($L)", variableName, UUID, tmpVarName)
                .endControlFlow();
    }

    private static void writeParcelable(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        String variableName = field.getName();
        String nullMarkerVariable = variableName.concat("Marker");
        String fieldAccessor = String.format("this.%s", field.getName());

        cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, fieldAccessor);
        cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
        cbBuilder.beginControlFlow("if($L != null)", fieldAccessor)
                .addStatement("$L.writeParcelable($L,0)", parcelVariableName, fieldAccessor)
                .endControlFlow();
    }

    private static void readParcelable(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeName typeName = field.getTypeInformation().getTypeName();
        String variableName = field.getName();
        String existenceVar = variableName.concat("Exists");

        cbBuilder.addStatement("$T $L = null", typeName, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .beginControlFlow("if($L >= $L)", ANDROID_SDK_INT, generateVersionCodeClassName("TIRAMISU"))
                .addStatement("$L = $L.readParcelable(this.getClass().getClassLoader(),$T.class)", variableName, parcelVariableName, typeName)
                .nextControlFlow("else")
                .addStatement("$L = $L.readParcelable(this.getClass().getClassLoader())", variableName, parcelVariableName)
                .endControlFlow()
                .endControlFlow();
    }

    private static void writeSparseArray(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        String variableName = field.getName();
        String nullMarkerVariable = variableName.concat("Marker");
        String fieldAccessor = String.format("this.%s", field.getName());

        cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, fieldAccessor);
        cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
        cbBuilder.beginControlFlow("if($L != null)", fieldAccessor)
                .addStatement("$L.writeSparseArray($L)", parcelVariableName, fieldAccessor)
                .endControlFlow();
    }

    private static void readSparseArray(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeInformation type = field.getTypeInformation();
        List<TypeInformation> typeArgs = type.getParametrizedTypes();
        TypeName typeName = type.getTypeName();
        TypeName valTypeName = typeArgs.get(0).getTypeName();
        String variableName = field.getName();
        String existenceVar = variableName.concat("Exists");

        cbBuilder.addStatement("$T $L = null", typeName, variableName)
                .addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName)
                .beginControlFlow("if($L)", existenceVar)
                .beginControlFlow("if($L >= $L)", ANDROID_SDK_INT, generateVersionCodeClassName("TIRAMISU"))
                .addStatement("$L = $L.readSparseArray(this.getClass().getClassLoader(),$T.class)", variableName, parcelVariableName, valTypeName)
                .nextControlFlow("else")
                .addStatement("$L = $L.readSparseArray(this.getClass().getClassLoader())", variableName, parcelVariableName)
                .endControlFlow()
                .endControlFlow();
    }

    private static void writeList(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        String variableName = field.getName();
        String nullMarkerVariable = variableName.concat("Marker");
        String fieldAccessor = String.format("this.%s", field.getName());

        cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, fieldAccessor);
        cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
        cbBuilder.beginControlFlow("if($L != null)", fieldAccessor)
                .addStatement("$L.writeList($L)", parcelVariableName, fieldAccessor)
                .endControlFlow();
    }

    private static void readList(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeInformation type = field.getTypeInformation();
        List<TypeInformation> typeArgs = type.getParametrizedTypes();
        TypeName typeName = type.getTypeName();
        TypeName valTypeName = typeArgs.get(0).getTypeName();
        String variableName = field.getName();
        String existenceVar = variableName.concat("Exists");

        cbBuilder.addStatement("$T $L = null", typeName, variableName)
                .addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName)
                .beginControlFlow("if($L)", existenceVar)
                .addStatement("$L = new $T()", variableName, type.canBeInstantiated() ? typeName : ARRAY_LIST)
                .beginControlFlow("if($L >= $L)", ANDROID_SDK_INT, generateVersionCodeClassName("TIRAMISU"))
                .addStatement("$L.readList($L,this.getClass().getClassLoader(),$T.class)", parcelVariableName, variableName, valTypeName)
                .nextControlFlow("else")
                .addStatement("$L.readList($L,this.getClass().getClassLoader())", parcelVariableName, variableName)
                .endControlFlow()
                .endControlFlow();
    }

    private static void writeMap(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        String variableName = field.getName();
        String nullMarkerVariable = variableName.concat("Marker");
        String fieldAccessor = String.format("this.%s", field.getName());

        cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, fieldAccessor);
        cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
        cbBuilder.beginControlFlow("if($L != null)", fieldAccessor)
                .addStatement("$L.writeMap($L)", parcelVariableName, fieldAccessor)
                .endControlFlow();
    }

    private static void readMap(CodeBlock.Builder cbBuilder, Field field, String parcelVariableName) {
        TypeInformation type = field.getTypeInformation();
        List<TypeInformation> typeArgs = type.getParametrizedTypes();
        TypeName typeName = type.getTypeName();
        TypeName keyTypeName = typeArgs.get(0).getTypeName();
        TypeName valTypeName = typeArgs.get(1).getTypeName();
        String variableName = field.getName();
        String existenceVar = variableName.concat("Exists");

        cbBuilder.addStatement("$T $L = null", typeName, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .addStatement("$L = new $T()", variableName, type.canBeInstantiated() ? typeName : HASH_MAP)
                .beginControlFlow("if($L >= $L)", ANDROID_SDK_INT, generateVersionCodeClassName("TIRAMISU"))
                .addStatement("$L.readMap($L, this.getClass().getClassLoader(),$T.class,$T.class)", parcelVariableName, variableName, keyTypeName, valTypeName)
                .nextControlFlow("else")
                .addStatement("$L.readMap($L, this.getClass().getClassLoader())", parcelVariableName, variableName)
                .endControlFlow()
                .endControlFlow();
    }
}