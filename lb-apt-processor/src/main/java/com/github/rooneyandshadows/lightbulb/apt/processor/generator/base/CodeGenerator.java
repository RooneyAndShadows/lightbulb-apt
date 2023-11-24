package com.github.rooneyandshadows.lightbulb.apt.processor.generator.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.inner.Parameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.inner.ClassField;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.Filer;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.*;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.TypeUtils.*;

public abstract class CodeGenerator {
    protected final Filer filer;
    protected final AnnotationResultsRegistry annotationResultsRegistry;


    public CodeGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        this.filer = filer;
        this.annotationResultsRegistry = annotationResultsRegistry;
    }

    public abstract void generate();

    protected CodeBlock generatePutIntoBundleBlockForParam(ClassField target, String bundleVarName, boolean useGetter) {
        String typeString = target.getType().toString();
        String parameterName = target.getName();
        String parameterAccessor = (target.hasGetter() && useGetter) ? target.getGetterName().concat("()") : parameterName;
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        boolean checkForNull = !target.getType().isPrimitive();

        if (checkForNull)
            codeBlock.beginControlFlow("if($L != null)", parameterAccessor);
        if (isString(typeString)) {
            codeBlock.addStatement(String.format("%s.putString($S,$L)", bundleVarName), parameterName, parameterAccessor);
        } else if (isUUID(typeString)) {
            codeBlock.addStatement(String.format("%s.putString($S,$L.toString())", bundleVarName), parameterName, parameterAccessor);
        } else if (isInt(typeString)) {
            codeBlock.addStatement(String.format("%s.putInt($S,$L)", bundleVarName), parameterName, parameterAccessor);
        } else if (isBoolean(typeString)) {
            codeBlock.addStatement(String.format("%s.putBoolean($S,$L)", bundleVarName), parameterName, parameterAccessor);
        } else if (isFloat(typeString)) {
            codeBlock.addStatement(String.format("%s.putFloat($S,$L)", bundleVarName), parameterName, parameterAccessor);
        } else if (isLong(typeString)) {
            codeBlock.addStatement(String.format("%s.putLong($S,$L.$L)", bundleVarName), parameterName, parameterAccessor);
        } else if (isDouble(typeString)) {
            codeBlock.addStatement(String.format("%s.putDouble($S,$L)", bundleVarName), parameterName, parameterAccessor);
        } else if (isDate(typeString)) {
            codeBlock.addStatement("$T $LDateString = $T.getDateString($T.$L,$L);", STRING, parameterName, DATE_UTILS, DATE_UTILS, "defaultFormat", parameterAccessor);
            codeBlock.addStatement(String.format("%s.putString($S,$L)", bundleVarName), parameterName, parameterName.concat("DateString"));
        } else if (isOffsetDate(typeString)) {
            codeBlock.addStatement("$T $LDateString = $T.getDateString($T.$L,$L);", STRING, parameterName, OFFSET_DATE_UTILS, OFFSET_DATE_UTILS, "defaultFormatWithTimeZone", parameterAccessor);
            codeBlock.addStatement(String.format("%s.putString($S,$L)", bundleVarName), parameterName, parameterName.concat("DateString"));
        } else {
            codeBlock.addStatement(String.format("%s.putParcelable($S,$L)", bundleVarName), parameterName, parameterAccessor);
        }
        if (checkForNull)
            codeBlock.endControlFlow();
        return codeBlock.build();
    }

    protected CodeBlock generateReadFromBundleBlockForParam(ClassField parameter, String bundleVariableName, boolean validateParameters) {
        String typeString = parameter.getType().toString();
        TypeName paramType = parameter.getType();
        String parameterName = parameter.getName();
        boolean isNullable = parameter.isNullable() || ((parameter instanceof Parameter) && ((Parameter) parameter).isOptional());
        boolean isPrimitive = parameter.getType().isPrimitive();
        boolean needsValidation = !isPrimitive && validateParameters && !isNullable;

        CodeBlock.Builder codeBlock = CodeBlock.builder();

        if (!needsValidation) {
            codeBlock.beginControlFlow("if($L.containsKey($S))", bundleVariableName, parameterName);
        } else {
            String errorMessage = String.format("Argument %s is not optional.", parameterName);
            codeBlock.beginControlFlow("if(!$L.containsKey($S))", bundleVariableName, parameterName)
                    .addStatement("throw new $T($S)", ClassNames.ILLEGAL_ARGUMENT_EXCEPTION, errorMessage)
                    .endControlFlow();
        }

        if (isSimpleType(typeString)) {
            if (isString(typeString)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getString($S,\"\")", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            } else if (isUUID(typeString)) {
                String bundleExp = String.format("$T $L = $T.fromString(%s.getString($S,\"\"))", bundleVariableName);
                codeBlock.addStatement(bundleExp, ClassNames.UUID, parameterName, ClassNames.UUID, parameterName);
            } else if (isInt(typeString)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getInt($S)", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            } else if (isBoolean(typeString)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getBoolean($S)", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            } else if (isFloat(typeString)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getFloat($S)", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            } else if (isLong(typeString)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getLong($S)", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            } else if (isDouble(typeString)) {
                String bundleExp = "$T $L = ".concat(String.format("%s.getDouble($S)", bundleVariableName));
                codeBlock.addStatement(bundleExp, paramType, parameterName, parameterName);
            }
        } else if (isDate(typeString)) {
            String getDateStringExpression = String.format("%s.getString($S)", bundleVariableName);
            codeBlock.addStatement("$T $LDateString = ".concat(getDateStringExpression).concat(""), STRING, parameterName, parameterName);
            codeBlock.addStatement("$T $L = $T.getDateFromString($T.$L,$L)", ClassNames.DATE, parameterName, DATE_UTILS, DATE_UTILS, "defaultFormat", parameterName.concat("DateString"));
            if (validateParameters) {
                String errorString = String.format("Argument %s is provided but date could not be parsed.", parameterName);
                codeBlock.beginControlFlow("if($L == null)", parameterName)
                        .addStatement("throw new $T($S)", ClassNames.ILLEGAL_ARGUMENT_EXCEPTION, errorString)
                        .endControlFlow();
            }
        } else if (isOffsetDate(typeString)) {
            String getDateStringExpression = String.format("%s.getString($S)", bundleVariableName);
            codeBlock.addStatement("$T $LDateString = ".concat(getDateStringExpression).concat(""), STRING, parameterName, parameterName);
            codeBlock.addStatement("$T $L = $T.getDateFromString($T.$L,$L)", ClassNames.OFFSET_DATE_TIME, parameterName, OFFSET_DATE_UTILS, OFFSET_DATE_UTILS, "defaultFormatWithTimeZone", parameterName.concat("DateString"));
            if (validateParameters) {
                String errorString = String.format("Argument %s is provided but date could not be parsed.", parameterName);
                codeBlock.beginControlFlow("if($L == null)", parameterName)
                        .addStatement("throw new $T($S)", ClassNames.ILLEGAL_ARGUMENT_EXCEPTION, errorString)
                        .endControlFlow();
            }
        } else {
            codeBlock.addStatement("$T $L", paramType, parameterName);
            codeBlock.beginControlFlow("if($L >= $L)", ClassNames.ANDROID_SDK_INT, ClassNames.generateVersionCodeClassName("TIRAMISU"))
                    .addStatement("$L = ".concat(String.format("%s.getParcelable($S,$L)", bundleVariableName)), parameterName, parameterName, paramType.toString().concat(".class"))
                    .nextControlFlow("else")
                    .addStatement("$L = ".concat(String.format("%s.getParcelable($S)", bundleVariableName)), parameterName, parameterName)
                    .endControlFlow();
            if (validateParameters) {
                String errorString = String.format("Argument %s is not nullable, but null value received from bundle.", parameterName);
                codeBlock.beginControlFlow("if($L == null)", parameterName)
                        .addStatement("throw new $T($S)", ClassNames.ILLEGAL_ARGUMENT_EXCEPTION, errorString)
                        .endControlFlow();
            }
        }

        if (parameter.hasSetter()) {
            codeBlock.addStatement("$L($L)", parameter.getSetterName(), parameterName);
        } else {
            codeBlock.addStatement("$L = $L", parameterName, parameterName);
        }
        if (!needsValidation) codeBlock.endControlFlow();
        return codeBlock.build();
    }
}
