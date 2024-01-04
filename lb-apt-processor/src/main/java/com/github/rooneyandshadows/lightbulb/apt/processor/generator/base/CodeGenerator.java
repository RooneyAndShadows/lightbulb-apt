package com.github.rooneyandshadows.lightbulb.apt.processor.generator.base;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.FragmentMetadata.ScreenParameter;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.ClassMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.base.FieldMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.*;
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
    protected final PackageNames packageNames;
    protected final ClassNames classNames;
    protected final ParcelableCodeGenerator parcelableCodeGenerator;
    protected final BundleCodeGenerator bundleCodeGenerator;
    private final AnnotationResultsRegistry annotationResultsRegistry;

    public CodeGenerator(Filer filer, Elements elements, PackageNames packageNames, ClassNames classNames, AnnotationResultsRegistry annotationResultsRegistry) {
        this.filer = filer;
        this.elements = elements;
        this.packageNames = packageNames;
        this.classNames = classNames;
        this.annotationResultsRegistry = annotationResultsRegistry;
        this.parcelableCodeGenerator = new ParcelableCodeGenerator(classNames);
        this.bundleCodeGenerator = new BundleCodeGenerator(classNames);
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
        return classNames.getClassName(activityMetadata);
    }

    @Nullable
    protected ClassName getSuperClassName(ClassMetadata metadata) {
        return classNames.getSuperClassName(metadata);
    }

    @NotNull
    protected ClassName getInstrumentedClassName(String packageName, ClassMetadata metadata, boolean prefix) {
        return classNames.generateInstrumentedClassName(packageName, metadata.getClassSimpleName(), prefix);
    }

    @NotNull
    protected ClassName getInstrumentedClassName(String packageName, ClassMetadata metadata) {
        return classNames.generateInstrumentedClassName(packageName, metadata.getClassSimpleName(), true);
    }

    protected void copyFieldsForSupertypeTransformation(List<? extends FieldMetadata> targets, List<FieldSpec> fields, List<MethodSpec> methods) {
        targets.forEach(fieldMetadata -> {
            boolean hasSetter = fieldMetadata.hasSetter();
            boolean hasGetter = fieldMetadata.hasGetter();
            TypeName fieldTypeName = classNames.getTypeName(fieldMetadata);

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
