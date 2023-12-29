package com.github.rooneyandshadows.lightbulb.apt.processor.generator.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.FragmentMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;

import java.io.IOException;
import java.lang.annotation.Annotation;

@SuppressWarnings("SameParameterValue")
public abstract class CodeGenerator {
    protected final Filer filer;
    protected final Elements elements;
    private final AnnotationResultsRegistry annotationResultsRegistry;

    public CodeGenerator(Filer filer, Elements elements, AnnotationResultsRegistry annotationResultsRegistry) {
        this.filer = filer;
        this.elements = elements;
        this.annotationResultsRegistry = annotationResultsRegistry;
    }

    protected abstract void generateCode(AnnotationResultsRegistry annotationResultsRegistry);

    protected abstract boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry);

    public final void generate() {
        if (willGenerateCode(annotationResultsRegistry)) {
            generateCode(annotationResultsRegistry);
        }
    }

    protected ParameterSpec generateParameterSpec(FragmentMetadata.Parameter parameter) {
        Field field = Field.from(parameter);
        TypeName fieldTypeName = field.getTypeInformation().getTypeName();
        String parameterName = field.getName();
        boolean isNullable = field.isNullable() || parameter.optional();

        return ParameterSpec.builder(fieldTypeName, parameterName)
                .addAnnotation(isNullable ? Nullable.class : NotNull.class)
                .build();
    }

    protected void writeClassFile(String packageName, TypeSpec.Builder typeSpecBuilder) {
        try {
            JavaFile.builder(packageName, typeSpecBuilder.build())
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    protected void generateFieldSetValueStatement(CodeBlock.Builder codeBlock, Field field, String fromVarName) {
        String accessorStatement = "this.%s";
        String varName = field.getName();

        if (field.hasSetter()) {
            String setterName = field.getSetterName();
            codeBlock.addStatement("$L($L)", String.format(accessorStatement, setterName), fromVarName);
        } else {
            codeBlock.addStatement("$L = $L", String.format(accessorStatement, varName), fromVarName);
        }
    }
}
