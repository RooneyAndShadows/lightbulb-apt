package com.github.rooneyandshadows.lightbulb.apt.processor.generator.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.inner.Parameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.inner.Variable;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.Filer;

import static com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.BundleCodeGenerator.*;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.*;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.TypeUtils.*;

@SuppressWarnings("SameParameterValue")
public abstract class CodeGenerator {
    protected final Filer filer;
    protected final AnnotationResultsRegistry annotationResultsRegistry;


    public CodeGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        this.filer = filer;
        this.annotationResultsRegistry = annotationResultsRegistry;
    }

    public abstract void generate();

    protected CodeBlock generatePutIntoBundleBlockForParam(
            Variable target,
            String bundleVarName,
            String variableContext,
            boolean useGetter
    ) {
        boolean hasContext = variableContext != null && !variableContext.isBlank();
        TypeName typeName = target.getType();
        String parameterName = target.getName();
        String contextStatement = hasContext ? variableContext.concat(".") : "";
        String accessorStatement = (target.hasGetter() && useGetter) ? target.getGetterName().concat("()") : parameterName;
        String variableAccessor = contextStatement.concat(accessorStatement);
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        generateWriteStatement(typeName, codeBlock, bundleVarName, variableAccessor, parameterName);

        return codeBlock.build();
    }

    protected CodeBlock generateReadFromBundleBlockForParam(
            Variable variable,
            String bundleVariableName,
            String variableContext,
            boolean validate
    ) {
        TypeName paramType = variable.getType();
        String varName = variable.getName();
        String tmpVarName = varName.concat("FromBundle");
        boolean isOptional = ((variable instanceof Parameter) && ((Parameter) variable).isOptional());
        boolean isNullable = variable.isNullable();
        boolean isNullableOrOptional = isNullable || isOptional;
        boolean isPrimitive = variable.getType().isPrimitive();
        boolean hasContext = variableContext != null && !variableContext.isBlank();
        boolean needsValidation = !isPrimitive && !isNullableOrOptional && validate;

        CodeBlock.Builder codeBlock = CodeBlock.builder();

        if (needsValidation) {
            codeBlock.beginControlFlow("if(!$L.containsKey($S))", bundleVariableName, varName)
                    .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, String.format("Argument %s is not optional.", varName))
                    .endControlFlow();

            generateReadStatement(paramType, codeBlock, bundleVariableName, tmpVarName, varName);

            codeBlock.beginControlFlow("if($L == null)", tmpVarName)
                    .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, String.format("%s can not be null but null value received from bundle.", varName))
                    .endControlFlow();

        } else {
            codeBlock.beginControlFlow("if($L.containsKey($S))", bundleVariableName, varName);
            generateReadStatement(paramType, codeBlock, bundleVariableName, tmpVarName, varName);
            codeBlock.endControlFlow();
        }

        String accessorStatement = hasContext ? variableContext.concat(".") : "";

        if (variable.hasSetter()) {
            codeBlock.addStatement("$L$L($L)", accessorStatement, variable.getSetterName(), tmpVarName);
        } else {
            codeBlock.addStatement("$L$L = $L", accessorStatement, varName, tmpVarName);
        }

        return codeBlock.build();
    }
}
