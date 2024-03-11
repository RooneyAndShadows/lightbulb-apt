package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.definitions.TypeDefinition;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.ParcelableMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation_metadata.base.FieldMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNameUtils;
import com.github.rooneyandshadows.lightbulb.apt.commons.PackageNames;
import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNameUtils.*;
import static javax.lang.model.element.Modifier.*;

@SuppressWarnings({"SameParameterValue", "DuplicatedCode"})
public class ParcelableGenerator extends CodeGenerator {
    private final List<ParcelableMetadata> parcelableDescriptions;

    public ParcelableGenerator(Filer filer, Elements elements, PackageNames packageNames, ClassNameUtils classNames, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, elements, packageNames, classNames, annotationResultsRegistry);
        parcelableDescriptions = annotationResultsRegistry.getParcelableDescriptions();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        parcelableDescriptions.forEach(parcelableMetadata -> {
            ClassName instrumentedClassName = getInstrumentedClassName(packageNames.getParcelablePackage(), parcelableMetadata);
            List<FieldSpec> fields = new ArrayList<>();
            List<MethodSpec> methods = new ArrayList<>();

            generateFields(parcelableMetadata, fields, methods);
            generateConstructorMethod(parcelableMetadata, methods);
            generateWriteToParcelMethod(parcelableMetadata, methods);
            generateDescribeContentsMethod(methods);

            TypeSpec.Builder parcelableClassBuilder = TypeSpec
                    .classBuilder(instrumentedClassName)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .addFields(fields)
                    .addMethods(methods);

            TypeDefinition superTypeInformation = parcelableMetadata.getType().getSuperClassType();

            if (superTypeInformation != null) {
                if (!superTypeInformation.is(ANDROID_PARCELABLE)) {
                    parcelableClassBuilder.addSuperinterface(ANDROID_PARCELABLE);
                } else {
                    parcelableClassBuilder.superclass(TypeName.get(superTypeInformation.getTypeMirror()));
                }
            }

            writeClassFile(instrumentedClassName.packageName(), parcelableClassBuilder);
        });
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.hasParcelableDescriptions();
    }

    private void generateFields(ParcelableMetadata parcelableMetadata, List<FieldSpec> fields, List<MethodSpec> methods) {
        List<? extends FieldMetadata> targets = parcelableMetadata.getTargetFields();
        copyFieldsForSupertypeTransformation(targets, fields, methods);
    }

    private void generateConstructorMethod(ParcelableMetadata parcelableMetadata, List<MethodSpec> methods) {
        MethodSpec.Builder constructorMethodBuilder = MethodSpec.constructorBuilder()
                .addModifiers(PROTECTED)
                .addParameter(ANDROID_PARCEL, "in");

        if (parcelableMetadata.superClassHasParcelConstructor()) {
            constructorMethodBuilder.addStatement("super(in)");
        }

        parcelableMetadata.getTargetFields().forEach(targetField -> {
            Field field = Field.from(targetField);

            CodeBlock readFromParcelBlock = parcelableCodeGenerator.generateReadStatement(field, "in");
            constructorMethodBuilder.addCode(readFromParcelBlock);
        });

        methods.add(constructorMethodBuilder.build());
    }


    private void generateDescribeContentsMethod(List<MethodSpec> methods) {
        MethodSpec describeContentsMethod = MethodSpec.methodBuilder("describeContents")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.INT)
                .addStatement("return 0")
                .build();

        methods.add(describeContentsMethod);
    }

    private void generateWriteToParcelMethod(ParcelableMetadata parcelableMetadata, List<MethodSpec> methods) {
        ParameterSpec parcelParam = ParameterSpec.builder(ANDROID_PARCEL, "dest")
                .addAnnotation(NotNull.class)
                .build();

        MethodSpec.Builder writeToParcelMethodBuilder = MethodSpec.methodBuilder("writeToParcel")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(parcelParam)
                .addParameter(TypeName.INT, "flags")
                .returns(void.class);

        parcelableMetadata.getTargetFields().forEach(targetField -> {
            Field field = Field.from(targetField);

            CodeBlock writeIntoParcelBlock = parcelableCodeGenerator.generateWriteStatement(field, "dest");
            writeToParcelMethodBuilder.addCode(writeIntoParcelBlock);
        });

        methods.add(writeToParcelMethodBuilder.build());
    }
}
