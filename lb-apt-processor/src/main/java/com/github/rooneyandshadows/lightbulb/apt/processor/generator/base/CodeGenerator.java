package com.github.rooneyandshadows.lightbulb.apt.processor.generator.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.Parameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.TypeInformation;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
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

    protected void generateFieldSetValueStatement(CodeBlock.Builder codeBlock, Field field, String fromVarName, boolean useSetter) {
        String accessorStatement = "this.%s";
        String varName = field.getName();

        if (useSetter && field.hasSetter()) {
            String setterName = field.getSetterName();
            codeBlock.addStatement("$L($L)", String.format(accessorStatement, setterName), fromVarName);
        } else {
            codeBlock.addStatement("$L = $L", String.format(accessorStatement, varName), fromVarName);
        }
    }
}
