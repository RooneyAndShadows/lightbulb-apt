package com.github.rooneyandshadows.lightbulb.apt.processor.generator.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.FragmentMetadata.ScreenParameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.ClassMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.FieldMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ElementUtils;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;

import java.io.IOException;
import java.util.List;

import static javax.lang.model.element.Modifier.*;
import static javax.lang.model.element.Modifier.ABSTRACT;

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

    @NotNull
    protected ClassName getClassName(ClassMetadata activityMetadata) {
        return ClassNames.getClassName(activityMetadata);
    }

    @Nullable
    protected ClassName getSuperClassName(ClassMetadata activityMetadata) {
        return ClassNames.getSuperClassName(activityMetadata);
    }

    @NotNull
    protected ClassName getInstrumentedClassName(String packageName, ClassMetadata activityMetadata, boolean prefix) {
        return ClassNames.generateInstrumentedClassName(packageName, activityMetadata.getClassSimpleName(), prefix);
    }

    @NotNull
    protected ClassName getInstrumentedClassName(String packageName, ClassMetadata activityMetadata) {
        return ClassNames.generateInstrumentedClassName(packageName, activityMetadata.getClassSimpleName(), true);
    }

    protected void copyFieldsForSupertypeTransformation(List<? extends FieldMetadata> targets, List<FieldSpec> fields, List<MethodSpec> methods) {
        targets.forEach(fieldMetadata -> {
            boolean hasSetter = fieldMetadata.hasSetter();
            boolean hasGetter = fieldMetadata.hasGetter();
            TypeName fieldTypeName = ClassNames.getTypeName(fieldMetadata);

            if (!hasGetter || !hasSetter) {
                Modifier fieldAccess = ElementUtils.accessModifierAtLeast(fieldMetadata.getElement(), PROTECTED);

                FieldSpec fieldSpec = FieldSpec.builder(fieldTypeName, fieldMetadata.getName())
                        .addModifiers(fieldAccess)
                        .build();
                fields.add(fieldSpec);
            }

            if (hasGetter) {
                Modifier getterAccess = fieldMetadata.getGetterAccessModifier() == PRIVATE ? PROTECTED : fieldMetadata.getGetterAccessModifier();

                MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder(fieldMetadata.getGetterName())
                        .addModifiers(getterAccess,ABSTRACT)
                        .returns(fieldTypeName);


                methods.add(getterBuilder.build());
            }

            if (hasSetter) {
                Modifier setterAccess = fieldMetadata.getSetterAccessModifier() == PRIVATE ? PROTECTED : fieldMetadata.getSetterAccessModifier();

                MethodSpec.Builder setterBuilder = MethodSpec.methodBuilder(fieldMetadata.getSetterName())
                        .addParameter(fieldTypeName, "value")
                        .addModifiers(setterAccess,ABSTRACT);

                methods.add(setterBuilder.build());
            }
        });
    }

    protected ParameterSpec generateFragmentScreenParameterSpec(ScreenParameter parameter) {
        TypeName fieldTypeName = TypeName.get(parameter.getTypeInformation().getTypeMirror());
        String parameterName = parameter.getName();
        boolean isNullable = parameter.isNullable() || parameter.isOptional();

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

}
