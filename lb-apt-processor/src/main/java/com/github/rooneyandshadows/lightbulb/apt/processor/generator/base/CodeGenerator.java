package com.github.rooneyandshadows.lightbulb.apt.processor.generator.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.Parameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.Filer;

import java.lang.annotation.Annotation;

import static com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.BundleCodeGenerator.*;

@SuppressWarnings("SameParameterValue")
public abstract class CodeGenerator {
    protected final Filer filer;
    private final AnnotationResultsRegistry annotationResultsRegistry;

    public CodeGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        this.filer = filer;
        this.annotationResultsRegistry = annotationResultsRegistry;
    }

    protected abstract void generateCode(AnnotationResultsRegistry annotationResultsRegistry);

    protected abstract boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry);

    public final void generate() {
        if (willGenerateCode(annotationResultsRegistry)) {
            generateCode(annotationResultsRegistry);
        }
    }

    public ParameterSpec generateParameterSpec(Parameter parameter) {
        boolean isNullable = parameter.isNullable() || parameter.isOptional();
        Class<? extends Annotation> nullabilityAnnotation = isNullable ? Nullable.class : NotNull.class;

        return ParameterSpec.builder(parameter.getTypeInformation().getTypeName(), parameter.getName())
                .addAnnotation(nullabilityAnnotation)
                .build();
    }

    protected CodeBlock generateWriteIntoBundleBlock(Field target, String bundleVarName, String variableContext, boolean useGetter) {
        boolean hasContext = variableContext != null && !variableContext.isBlank();
        String parameterName = target.getName();
        String contextStatement = hasContext ? variableContext.concat(".") : "";
        String accessorStatement = (target.hasGetter() && useGetter) ? target.getGetterName().concat("()") : parameterName;
        String variableAccessor = contextStatement.concat(accessorStatement);
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        generateWriteStatement(target.getTypeInformation(), codeBlock, bundleVarName, variableAccessor, parameterName);

        return codeBlock.build();
    }

    protected CodeBlock generateReadFromBundleBlock(Field variable, String bundleVariableName, String variableContext, boolean useSetter) {
        String varName = variable.getName();
        String tmpVarName = varName.concat("FromBundle");

        CodeBlock.Builder codeBlock = CodeBlock.builder();

        codeBlock.beginControlFlow("if($L.containsKey($S))", bundleVariableName, varName);
        generateReadStatement(variable.getTypeInformation(), codeBlock, bundleVariableName, tmpVarName, varName);
        generateVariableSetValueStatement(codeBlock, variable, variableContext, tmpVarName, useSetter);
        codeBlock.endControlFlow();

        return codeBlock.build();
    }

    protected void generateVariableSetValueStatement(CodeBlock.Builder codeBlock, Field variable, String variableContext, String fromVarName, boolean useSetter) {
        boolean hasContext = variableContext != null && !variableContext.isBlank();
        String accessorStatement = hasContext ? variableContext.concat(".") : "";
        String varName = variable.getName();

        if (useSetter && variable.hasSetter()) {
            String setterName = variable.getSetterName();
            codeBlock.addStatement("$L($L)", accessorStatement.concat(setterName), fromVarName);
        } else {
            codeBlock.addStatement("$L = $L", accessorStatement.concat(varName), fromVarName);
        }
    }
}
