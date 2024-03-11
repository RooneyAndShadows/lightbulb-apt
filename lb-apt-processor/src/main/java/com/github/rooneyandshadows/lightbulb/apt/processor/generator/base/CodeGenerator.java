package com.github.rooneyandshadows.lightbulb.apt.processor.generator.base;

import com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.FragmentMetadata.ScreenParameterMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.ClassMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.FieldMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.MethodMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.ParameterDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.TypeDefinition;
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

@SuppressWarnings("SameParameterValue")
public abstract class CodeGenerator {
    protected final Filer filer;
    protected final Elements elements;
    protected final PackageNames packageNames;
    protected final ClassNameUtils classNames;
    protected final ParcelableCodeGenerator parcelableCodeGenerator;
    protected final BundleCodeGenerator bundleCodeGenerator;
    private final AnnotationResultsRegistry annotationResultsRegistry;

    public CodeGenerator(Filer filer, Elements elements, PackageNames packageNames, ClassNameUtils classNames, AnnotationResultsRegistry annotationResultsRegistry) {
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
        return classNames.generateInstrumentedClassName(packageName, metadata.getResolvedSimpleName(), prefix);
    }

    @NotNull
    protected ClassName getInstrumentedClassName(String packageName, ClassMetadata metadata) {
        return classNames.generateInstrumentedClassName(packageName, metadata.getResolvedSimpleName(), true);
    }

    protected void copyMethodsForSupertypeTransformation(List<? extends MethodMetadata> targets, List<MethodSpec> methods) {
        targets.forEach(methodMetadata -> {
            Modifier methodAccess = methodMetadata.getAccessModifier() == PRIVATE ? PROTECTED : methodMetadata.getAccessModifier();
            TypeName returnType = classNames.getTypeName(methodMetadata.getMethod().getReturnType());


            MethodSpec.Builder builder = MethodSpec.methodBuilder(methodMetadata.getMethod().getName())
                    .addModifiers(methodAccess, ABSTRACT)
                    .returns(returnType);

            methodMetadata.getMethod().getParameters(true).forEach(parameterDefinition -> {
                builder.addParameter(generateParameterSpec(parameterDefinition));
            });


            methods.add(builder.build());

        });
    }

    protected void copyFieldsForSupertypeTransformation(List<? extends FieldMetadata> targets, List<FieldSpec> fields, List<MethodSpec> methods) {
        targets.forEach(fieldMetadata -> {
            boolean hasSetter = fieldMetadata.hasSetter();
            boolean hasGetter = fieldMetadata.hasGetter();
            TypeName fieldTypeName = classNames.getTypeName(fieldMetadata);

            if (!hasGetter || !hasSetter) {
                Modifier fieldAccess = ElementUtils.accessModifierAtLeast(fieldMetadata.getAccessModifier(), PROTECTED);

                FieldSpec fieldSpec = FieldSpec.builder(fieldTypeName, fieldMetadata.getName())
                        .addModifiers(fieldAccess)
                        .build();
                fields.add(fieldSpec);
            }

            if (hasGetter) {
                Modifier getterAccess = fieldMetadata.getGetterAccessModifier() == PRIVATE ? PROTECTED : fieldMetadata.getGetterAccessModifier();

                MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder(fieldMetadata.getGetterName())
                        .addModifiers(getterAccess, ABSTRACT)
                        .returns(fieldTypeName);


                methods.add(getterBuilder.build());
            }

            if (hasSetter) {
                Modifier setterAccess = fieldMetadata.getSetterAccessModifier() == PRIVATE ? PROTECTED : fieldMetadata.getSetterAccessModifier();

                MethodSpec.Builder setterBuilder = MethodSpec.methodBuilder(fieldMetadata.getSetterName())
                        .addParameter(fieldTypeName, "value")
                        .addModifiers(setterAccess, ABSTRACT);

                methods.add(setterBuilder.build());
            }
        });
    }

    protected ParameterSpec generateScreenParameterSpec(ScreenParameterMetadata parameter) {
        return generateParameterSpec(parameter.getName(), parameter.getType(), parameter.isNullable() || parameter.isOptional());
    }

    protected ParameterSpec generateParameterSpec(ParameterDefinition parameterDefinition) {
        return generateParameterSpec(parameterDefinition.getName(), parameterDefinition.getType(), parameterDefinition.isNullable());
    }

    protected ParameterSpec generateParameterSpec(String name, TypeDefinition type, boolean isNullable) {
        TypeName fieldTypeName = TypeName.get(type.getTypeMirror());

        return ParameterSpec.builder(fieldTypeName, name)
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
