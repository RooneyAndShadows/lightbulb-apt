package com.github.rooneyandshadows.lightbulb.apt.processor.generator.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.fragment.inner.Variable;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.Filer;

import static com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.BundleCodeGenerator.*;

@SuppressWarnings("SameParameterValue")
public abstract class CodeGenerator {
    protected final Filer filer;
    protected final AnnotationResultsRegistry annotationResultsRegistry;


    public CodeGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        this.filer = filer;
        this.annotationResultsRegistry = annotationResultsRegistry;
    }

    public abstract void generate();

    protected CodeBlock generateWriteIntoBundleBlock(Variable target, String bundleVarName, String variableContext, boolean useGetter) {
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

    protected CodeBlock generateReadFromBundleBlock(Variable variable, String bundleVariableName, String variableContext, boolean useSetter) {
        TypeName paramType = variable.getType();
        String varName = variable.getName();
        String tmpVarName = varName.concat("FromBundle");

        CodeBlock.Builder codeBlock = CodeBlock.builder();

        codeBlock.beginControlFlow("if($L.containsKey($S))", bundleVariableName, varName);
        generateReadStatement(paramType, codeBlock, bundleVariableName, tmpVarName, varName);
        generateVariableSetValueStatement(codeBlock, variable, variableContext, tmpVarName, useSetter);
        codeBlock.endControlFlow();

        return codeBlock.build();
    }

    protected void generateVariableSetValueStatement(CodeBlock.Builder codeBlock, Variable variable, String variableContext, String fromVarName, boolean useSetter) {
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
