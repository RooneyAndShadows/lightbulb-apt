package com.github.rooneyandshadows.lightbulb.apt.processor.utils;

import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.base.DeclaredValueHolder;
import com.github.rooneyandshadows.lightbulb.apt.processor.TypeInformation;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNameUtils.*;
import static com.squareup.javapoet.TypeName.*;

@SuppressWarnings("DuplicatedCode")
public class ParcelableCodeGenerator {
    private final ClassNameUtils classNames;

    public ParcelableCodeGenerator(ClassNameUtils classNames) {
        this.classNames = classNames;
    }

    public CodeBlock generateWriteStatement(DeclaredValueHolder valueHolder, String bundleVariableName) {
        TypeInformation type = valueHolder.getTypeInformation();

        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

        if (type.isString()) {
            writeString(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isUUID()) {
            writeUUID(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isInt()) {
            writeInt(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isBoolean()) {
            writeBoolean(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isFloat()) {
            writeFloat(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isLong()) {
            writeLong(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isDouble()) {
            writeDouble(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isDate()) {
            writeDate(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isOffsetDate()) {
            writeOffsetDate(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.is(ANDROID_PARCELABLE)) {
            writeParcelable(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.is(ANDROID_SPARSE_ARRAY)) {
            writeSparseArray(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isList()) {
            writeList(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isMap()) {
            writeMap(codeBlockBuilder, valueHolder, bundleVariableName);
        } else {
            writeValue(codeBlockBuilder, valueHolder, bundleVariableName);
        }

        return codeBlockBuilder.build();
    }

    public CodeBlock generateReadStatement(DeclaredValueHolder valueHolder, String bundleVariableName) {
        TypeInformation type = valueHolder.getTypeInformation();

        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

        if (type.isString()) {
            readString(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isUUID()) {
            readUUID(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isInt()) {
            readInt(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isBoolean()) {
            readBoolean(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isFloat()) {
            readFloat(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isLong()) {
            readLong(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isDouble()) {
            readDouble(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isDate()) {
            readDate(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isOffsetDate()) {
            readOffsetDate(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.is(ANDROID_PARCELABLE)) {
            readParcelable(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.is(ANDROID_SPARSE_ARRAY)) {
            readSparseArray(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isList()) {
            readList(codeBlockBuilder, valueHolder, bundleVariableName);
        } else if (type.isMap()) {
            readMap(codeBlockBuilder, valueHolder, bundleVariableName);
        } else {
            readValue(codeBlockBuilder, valueHolder, bundleVariableName);
        }

        return codeBlockBuilder.build();
    }

    private void writeOffsetDate(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        String variableName = valueHolder.getName();
        String nullMarkerVariable = variableName.concat("Marker");
        String tmpVarName = variableName.concat("OffsetDateString");
        String valueAccessor = valueHolder.getValueAccessor();

        cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, valueAccessor);
        cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
        cbBuilder.beginControlFlow("if($L != null)", valueAccessor)
                .addStatement("$T $L = $T.getOffsetDateString($L)", STRING, tmpVarName, DATE_UTILS, valueAccessor)
                .addStatement("$L.writeString($L)", parcelVariableName, tmpVarName)
                .endControlFlow();
    }

    private void readOffsetDate(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeName typeName = TypeName.get(valueHolder.getTypeInformation().getTypeMirror());
        String variableName = valueHolder.getName();
        String existenceVar = variableName.concat("Exists");
        String tmpVarName = variableName.concat("OffsetDateString");
        String setStatement = valueHolder.getValueSetStatement(variableName);

        cbBuilder.addStatement("$T $L = null", typeName, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .addStatement("$T $L = $L.readString()", STRING, tmpVarName, parcelVariableName)
                .addStatement("$L = $T.getOffsetDateFromString($L)", variableName, DATE_UTILS, tmpVarName)
                .endControlFlow()
                .addStatement(setStatement);
    }

    private void writeDate(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        String tmpVarName = "dateString";
        String variableName = valueHolder.getName();
        String nullMarkerVariable = variableName.concat("Marker");
        String valueAccessor = valueHolder.getValueAccessor();

        cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, valueAccessor);
        cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
        cbBuilder.beginControlFlow("if($L != null)", valueAccessor)
                .addStatement("$T $L = $T.getDateString($L)", STRING, tmpVarName, DATE_UTILS, valueAccessor)
                .addStatement("$L.writeString($L)", parcelVariableName, tmpVarName)
                .endControlFlow();
    }

    private void readDate(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeName typeName = TypeName.get(valueHolder.getTypeInformation().getTypeMirror());
        String variableName = valueHolder.getName();
        String existenceVar = variableName.concat("Exists");
        String tmpVarName = variableName.concat("DateString");
        String setStatement = valueHolder.getValueSetStatement(variableName);

        cbBuilder.addStatement("$T $L = null", typeName, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .addStatement("$T $L = $L.readString()", STRING, tmpVarName, parcelVariableName)
                .addStatement("$L = $T.getDateFromString($L)", variableName, DATE_UTILS, tmpVarName)
                .endControlFlow()
                .addStatement(setStatement);
    }

    private void writeInt(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeName typeName = TypeName.get(valueHolder.getTypeInformation().getTypeMirror());
        String valueAccessor = valueHolder.getValueAccessor();
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$L.writeInt($L)", parcelVariableName, valueAccessor);
        } else {
            String variableName = valueHolder.getName();
            String nullMarkerVariable = variableName.concat("Marker");

            cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, valueAccessor);
            cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
            cbBuilder.beginControlFlow("if($L != null)", valueAccessor)
                    .addStatement("$L.writeInt($L)", parcelVariableName, valueAccessor)
                    .endControlFlow();
        }
    }

    private void readInt(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeName typeName = TypeName.get(valueHolder.getTypeInformation().getTypeMirror());
        String variableName = valueHolder.getName();
        String setStatement = valueHolder.getValueSetStatement(variableName);
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$T $L = $L.readInt()", typeName, variableName, parcelVariableName)
                    .addStatement(setStatement);
        } else {
            String existenceVar = variableName.concat("Exists");
            cbBuilder.addStatement("$T $L = null", typeName, variableName);
            cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
            cbBuilder.beginControlFlow("if($L)", existenceVar)
                    .addStatement("$L = $L.readInt()", variableName, parcelVariableName)
                    .endControlFlow()
                    .addStatement(setStatement);
        }
    }

    private void writeString(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        String variableName = valueHolder.getName();
        String nullMarkerVariable = variableName.concat("Marker");
        String valueAccessor = valueHolder.getValueAccessor();

        cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, valueAccessor);
        cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
        cbBuilder.beginControlFlow("if($L != null)", valueAccessor)
                .addStatement("$L.writeString($L)", parcelVariableName, valueAccessor)
                .endControlFlow();
    }

    private void readString(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeName typeName = TypeName.get(valueHolder.getTypeInformation().getTypeMirror());
        String variableName = valueHolder.getName();
        String existenceVar = variableName.concat("Exists");
        String setStatement = valueHolder.getValueSetStatement(variableName);

        cbBuilder.addStatement("$T $L = null", typeName, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .addStatement("$L = $L.readString()", variableName, parcelVariableName)
                .endControlFlow()
                .addStatement(setStatement);
    }

    private void writeDouble(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeName typeName = TypeName.get(valueHolder.getTypeInformation().getTypeMirror());
        String valueAccessor = valueHolder.getValueAccessor();
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$L.writeDouble($L)", parcelVariableName, valueAccessor);
        } else {
            String variableName = valueHolder.getName();
            String nullMarkerVariable = variableName.concat("Marker");

            cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, valueAccessor);
            cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
            cbBuilder.beginControlFlow("if($L != null)", valueAccessor)
                    .addStatement("$L.writeDouble($L)", parcelVariableName, valueAccessor)
                    .endControlFlow();
        }
    }

    private void readDouble(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeName typeName = TypeName.get(valueHolder.getTypeInformation().getTypeMirror());
        String variableName = valueHolder.getName();
        String setStatement = valueHolder.getValueSetStatement(variableName);
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$T $L = $L.readDouble()", typeName, variableName, parcelVariableName)
                    .addStatement(setStatement);
        } else {
            String existenceVar = variableName.concat("Exists");
            cbBuilder.addStatement("$T $L = null", typeName, variableName);
            cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
            cbBuilder.beginControlFlow("if($L)", existenceVar)
                    .addStatement("$L = $L.readDouble()", variableName, parcelVariableName)
                    .endControlFlow()
                    .addStatement(setStatement);
        }
    }

    private void writeFloat(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeName typeName = TypeName.get(valueHolder.getTypeInformation().getTypeMirror());
        String valueAccessor = valueHolder.getValueAccessor();
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$L.writeFloat($L)", parcelVariableName, valueAccessor);
        } else {
            String variableName = valueHolder.getName();
            String nullMarkerVariable = variableName.concat("Marker");

            cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, valueAccessor);
            cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
            cbBuilder.beginControlFlow("if($L != null)", valueAccessor)
                    .addStatement("$L.writeFloat($L)", parcelVariableName, valueAccessor)
                    .endControlFlow();
        }
    }

    private void readFloat(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeName typeName = TypeName.get(valueHolder.getTypeInformation().getTypeMirror());
        String variableName = valueHolder.getName();
        String setStatement = valueHolder.getValueSetStatement(variableName);
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$T $L = $L.readFloat()", typeName, variableName, parcelVariableName)
                    .addStatement(setStatement);
        } else {
            String existenceVar = variableName.concat("Exists");
            cbBuilder.addStatement("$T $L = null", typeName, variableName);
            cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
            cbBuilder.beginControlFlow("if($L)", existenceVar)
                    .addStatement("$L = $L.readFloat()", variableName, parcelVariableName)
                    .endControlFlow()
                    .addStatement(setStatement);
        }
    }

    private void writeLong(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeName typeName = TypeName.get(valueHolder.getTypeInformation().getTypeMirror());
        String valueAccessor = valueHolder.getValueAccessor();
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$L.writeLong($L)", parcelVariableName, valueAccessor);
        } else {
            String variableName = valueHolder.getName();
            String nullMarkerVariable = variableName.concat("Marker");

            cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, valueAccessor);
            cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
            cbBuilder.beginControlFlow("if($L != null)", valueAccessor)
                    .addStatement("$L.writeLong($L)", parcelVariableName, valueAccessor)
                    .endControlFlow();
        }
    }

    private void readLong(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeName typeName = TypeName.get(valueHolder.getTypeInformation().getTypeMirror());
        String variableName = valueHolder.getName();
        String setStatement = valueHolder.getValueSetStatement(variableName);
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$T $L = $L.readLong()", typeName, variableName, parcelVariableName)
                    .addStatement(setStatement);
        } else {
            String existenceVar = variableName.concat("Exists");
            cbBuilder.addStatement("$T $L = null", typeName, variableName);
            cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
            cbBuilder.beginControlFlow("if($L)", existenceVar)
                    .addStatement("$L = $L.readLong()", variableName, parcelVariableName)
                    .endControlFlow()
                    .addStatement(setStatement);
        }
    }

    private void writeBoolean(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeName typeName = TypeName.get(valueHolder.getTypeInformation().getTypeMirror());
        String valueAccessor = valueHolder.getValueAccessor();
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$L.writeInt($L ? 1 : 0)", parcelVariableName, valueAccessor);
        } else {
            String variableName = valueHolder.getName();
            String nullMarkerVariable = variableName.concat("Marker");

            cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, valueAccessor);
            cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
            cbBuilder.beginControlFlow("if($L != null)", valueAccessor)
                    .addStatement("$L.writeInt($L ? 1 : 0)", parcelVariableName, valueAccessor)
                    .endControlFlow();
        }
    }

    private void readBoolean(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeName typeName = TypeName.get(valueHolder.getTypeInformation().getTypeMirror());
        String variableName = valueHolder.getName();
        String setStatement = valueHolder.getValueSetStatement(variableName);
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$T $L = $L.readInt() == 1", typeName, variableName, parcelVariableName)
                    .addStatement(setStatement);
        } else {
            String existenceVar = variableName.concat("Exists");
            cbBuilder.addStatement("$T $L = null", typeName, variableName);
            cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
            cbBuilder.beginControlFlow("if($L)", existenceVar)
                    .addStatement("$L = $L.readInt() == 1", variableName, parcelVariableName)
                    .endControlFlow()
                    .addStatement(setStatement);
        }
    }

    private void writeUUID(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        String variableName = valueHolder.getName();
        String nullMarkerVariable = variableName.concat("Marker");
        String tmpVarName = variableName.concat("uuidString");
        String valueAccessor = valueHolder.getValueAccessor();

        cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, valueAccessor);
        cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
        cbBuilder.beginControlFlow("if($L != null)", valueAccessor)
                .addStatement("$T $L = $L.toString()", STRING, tmpVarName, valueAccessor)
                .addStatement("$L.writeString($L)", parcelVariableName, tmpVarName)
                .endControlFlow();
    }

    private void readUUID(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeName typeName = TypeName.get(valueHolder.getTypeInformation().getTypeMirror());
        String variableName = valueHolder.getName();
        String setStatement = valueHolder.getValueSetStatement(variableName);
        String existenceVar = variableName.concat("Exists");
        String tmpVarName = variableName.concat("UuidString");

        cbBuilder.addStatement("$T $L = null", typeName, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .addStatement("$T $L = $L.readString()", STRING, tmpVarName, parcelVariableName)
                .addStatement("$L = $T.fromString($L)", variableName, UUID, tmpVarName)
                .endControlFlow()
                .addStatement(setStatement);
    }

    private void writeParcelable(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        String variableName = valueHolder.getName();
        String nullMarkerVariable = variableName.concat("Marker");
        String valueAccessor = valueHolder.getValueAccessor();

        cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, valueAccessor);
        cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
        cbBuilder.beginControlFlow("if($L != null)", valueAccessor)
                .addStatement("$L.writeParcelable($L,0)", parcelVariableName, valueAccessor)
                .endControlFlow();
    }

    private void readParcelable(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeName typeName = TypeName.get(valueHolder.getTypeInformation().getTypeMirror());
        String variableName = valueHolder.getName();
        String setStatement = valueHolder.getValueSetStatement(variableName);
        String existenceVar = variableName.concat("Exists");

        cbBuilder.addStatement("$T $L = null", typeName, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .beginControlFlow("if($L >= $L)", ANDROID_SDK_INT, classNames.generateVersionCodeClassName("TIRAMISU"))
                .addStatement("$L = $L.readParcelable(this.getClass().getClassLoader(),$T.class)", variableName, parcelVariableName, typeName)
                .nextControlFlow("else")
                .addStatement("$L = $L.readParcelable(this.getClass().getClassLoader())", variableName, parcelVariableName)
                .endControlFlow()
                .endControlFlow()
                .addStatement(setStatement);
    }

    private void writeSparseArray(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        String variableName = valueHolder.getName();
        String nullMarkerVariable = variableName.concat("Marker");
        String valueAccessor = valueHolder.getValueAccessor();

        cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, valueAccessor);
        cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
        cbBuilder.beginControlFlow("if($L != null)", valueAccessor)
                .addStatement("$L.writeSparseArray($L)", parcelVariableName, valueAccessor)
                .endControlFlow();
    }

    private void readSparseArray(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeInformation type = valueHolder.getTypeInformation();
        List<TypeInformation> typeArgs = type.getParametrizedTypes();
        TypeName typeName = TypeName.get(type.getTypeMirror());
        TypeName valTypeName = TypeName.get(typeArgs.get(0).getTypeMirror());
        String variableName = valueHolder.getName();
        String setStatement = valueHolder.getValueSetStatement(variableName);
        String existenceVar = variableName.concat("Exists");

        cbBuilder.addStatement("$T $L = null", typeName, variableName)
                .addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName)
                .beginControlFlow("if($L)", existenceVar)
                .beginControlFlow("if($L >= $L)", ANDROID_SDK_INT, classNames.generateVersionCodeClassName("TIRAMISU"))
                .addStatement("$L = $L.readSparseArray(this.getClass().getClassLoader(),$T.class)", variableName, parcelVariableName, valTypeName)
                .nextControlFlow("else")
                .addStatement("$L = $L.readSparseArray(this.getClass().getClassLoader())", variableName, parcelVariableName)
                .endControlFlow()
                .endControlFlow()
                .addStatement(setStatement);
    }

    private void writeList(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        String variableName = valueHolder.getName();
        String nullMarkerVariable = variableName.concat("Marker");
        String valueAccessor = valueHolder.getValueAccessor();

        cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, valueAccessor);
        cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
        cbBuilder.beginControlFlow("if($L != null)", valueAccessor)
                .addStatement("$L.writeList($L)", parcelVariableName, valueAccessor)
                .endControlFlow();
    }

    private void readList(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeInformation type = valueHolder.getTypeInformation();
        List<TypeInformation> typeArgs = type.getParametrizedTypes();
        TypeName typeName = TypeName.get(type.getTypeMirror());
        TypeName valTypeName = TypeName.get(typeArgs.get(0).getTypeMirror());
        String variableName = valueHolder.getName();
        String setStatement = valueHolder.getValueSetStatement(variableName);
        String existenceVar = variableName.concat("Exists");

        cbBuilder.addStatement("$T $L = null", typeName, variableName)
                .addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName)
                .beginControlFlow("if($L)", existenceVar)
                .addStatement("$L = new $T()", variableName, type.canBeInstantiated() ? typeName : ARRAY_LIST)
                .beginControlFlow("if($L >= $L)", ANDROID_SDK_INT, classNames.generateVersionCodeClassName("TIRAMISU"))
                .addStatement("$L.readList($L,this.getClass().getClassLoader(),$T.class)", parcelVariableName, variableName, valTypeName)
                .nextControlFlow("else")
                .addStatement("$L.readList($L,this.getClass().getClassLoader())", parcelVariableName, variableName)
                .endControlFlow()
                .endControlFlow()
                .addStatement(setStatement);
    }

    private void writeMap(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        String variableName = valueHolder.getName();
        String nullMarkerVariable = variableName.concat("Marker");
        String valueAccessor = valueHolder.getValueAccessor();

        cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, valueAccessor);
        cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
        cbBuilder.beginControlFlow("if($L != null)", valueAccessor)
                .addStatement("$L.writeMap($L)", parcelVariableName, valueAccessor)
                .endControlFlow();
    }

    private void readMap(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeInformation type = valueHolder.getTypeInformation();
        List<TypeInformation> typeArgs = type.getParametrizedTypes();
        TypeName typeName = TypeName.get(type.getTypeMirror());
        TypeName keyTypeName = TypeName.get(typeArgs.get(0).getTypeMirror());
        TypeName valTypeName = TypeName.get(typeArgs.get(1).getTypeMirror());
        String variableName = valueHolder.getName();
        String setStatement = valueHolder.getValueSetStatement(variableName);
        String existenceVar = variableName.concat("Exists");

        cbBuilder.addStatement("$T $L = null", typeName, variableName);
        cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
        cbBuilder.beginControlFlow("if($L)", existenceVar)
                .addStatement("$L = new $T()", variableName, type.canBeInstantiated() ? typeName : HASH_MAP)
                .beginControlFlow("if($L >= $L)", ANDROID_SDK_INT, classNames.generateVersionCodeClassName("TIRAMISU"))
                .addStatement("$L.readMap($L, this.getClass().getClassLoader(),$T.class,$T.class)", parcelVariableName, variableName, keyTypeName, valTypeName)
                .nextControlFlow("else")
                .addStatement("$L.readMap($L, this.getClass().getClassLoader())", parcelVariableName, variableName)
                .endControlFlow()
                .endControlFlow()
                .addStatement(setStatement);
    }

    private void writeValue(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeName typeName = TypeName.get(valueHolder.getTypeInformation().getTypeMirror());
        String valueAccessor = valueHolder.getValueAccessor();
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$L.writeValue($L)", parcelVariableName, valueAccessor);
        } else {
            String variableName = valueHolder.getName();
            String nullMarkerVariable = variableName.concat("Marker");

            cbBuilder.addStatement("$T $L = ($T)($L == null ? 0 : 1)", BYTE, nullMarkerVariable, BYTE, valueAccessor);
            cbBuilder.addStatement("$L.writeByte($L)", parcelVariableName, nullMarkerVariable);
            cbBuilder.beginControlFlow("if($L != null)", valueAccessor)
                    .addStatement("$L.writeValue($L)", parcelVariableName, valueAccessor)
                    .endControlFlow();
        }
    }

    private void readValue(CodeBlock.Builder cbBuilder, DeclaredValueHolder valueHolder, String parcelVariableName) {
        TypeName typeName = TypeName.get(valueHolder.getTypeInformation().getTypeMirror());
        String variableName = valueHolder.getName();
        String setStatement = valueHolder.getValueSetStatement(variableName);
        boolean isPrimitive = typeName.isPrimitive();

        if (isPrimitive) {
            cbBuilder.addStatement("$T $L = $L.readValue(this.getClass().getClassLoader())", typeName, variableName, parcelVariableName)
                    .addStatement(setStatement);
        } else {
            String existenceVar = variableName.concat("Exists");
            cbBuilder.addStatement("$T $L = null", typeName, variableName);
            cbBuilder.addStatement("$T $L = (($T)$L.readByte()) == 1", BOOLEAN, existenceVar, INT, parcelVariableName);
            cbBuilder.beginControlFlow("if($L)", existenceVar)
                    .addStatement("$L = $L.readValue(this.getClass().getClassLoader())", variableName, parcelVariableName)
                    .endControlFlow()
                    .addStatement(setStatement);
        }
    }
}