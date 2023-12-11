package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.LightbulbStorageDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.StringUtils;
import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry.AnnotationResultTypes.LIGHTBULB_STORAGE_DESCRIPTION;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.*;
import static javax.lang.model.element.Modifier.*;

public class StorageGenerator extends CodeGenerator {
    private final String storagePackage;

    public StorageGenerator(Filer filer, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, annotationResultsRegistry);
        storagePackage = PackageNames.getStoragePackage();
    }

    @Override
    public void generate() {
        List<LightbulbStorageDescription> storageDescriptions = annotationResultsRegistry.getResult(LIGHTBULB_STORAGE_DESCRIPTION);
        storageDescriptions.forEach(storageDescription -> {
            generateStorageImplementation(storageDescription);
        });
    }

    private void generateStorageImplementation(LightbulbStorageDescription storageDescription) {
        ClassName instrumentedClassName = storageDescription.getInstrumentedClassName();
        ClassName storageClassName = storageDescription.getClassName();
        ParameterizedTypeName superClass = ParameterizedTypeName.get(
                BASE_STORAGE,
                storageDescription.getClassName());

        String storageKeyFieldInitializer = String.format("%s.%s", PackageNames.getRootPackage(), storageDescription.getName());

        for (int index = 0; index < storageDescription.getSubKeys().length; index++) {
            String keyString = String.format(Locale.getDefault(), "$%s", index);
            storageKeyFieldInitializer = storageKeyFieldInitializer.concat(keyString);
        }

        FieldSpec fieldSpec = FieldSpec.builder(STRING, "STORAGE_KEY", PRIVATE, FINAL)
                .initializer("$S", storageKeyFieldInitializer)
                .build();

        ParameterizedTypeName storageClassTypeName = ParameterizedTypeName.get(CLASS, storageClassName);

        MethodSpec getStorageClassMethod = MethodSpec.methodBuilder("getStorageClass")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .returns(storageClassTypeName)
                .addStatement("return $T.class", storageClassName)
                .build();

        MethodSpec getDefaultMethod = MethodSpec.methodBuilder("getDefault")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addAnnotation(NotNull.class)
                .returns(storageDescription.getClassName())
                .addStatement("return new $T()", storageDescription.getClassName())
                .build();

        List<MethodSpec> storageFieldAccessors = new ArrayList<>();

        storageDescription.getFields().forEach(field -> {
            String fieldName = field.getName();
            String capitalizedFieldName = StringUtils.capitalizeFirstLetter(fieldName);
            String getterName = String.format("get%s", capitalizedFieldName);
            String setterName = String.format("set%s", capitalizedFieldName);
            String[] subKeys = storageDescription.getSubKeys();
            List<ParameterSpec> keyParameters = new ArrayList<>();
            String keyParamsCommaSeparated = "";

            for (int i = 0; i < subKeys.length; i++) {
                boolean isLast = i == subKeys.length - 1;
                String subKey = subKeys[i];

                ParameterSpec param = ParameterSpec.builder(STRING, subKey)
                        .addAnnotation(NotNull.class)
                        .build();

                keyParameters.add(param);

                keyParamsCommaSeparated = keyParamsCommaSeparated.concat(subKey);

                if (!isLast) {
                    keyParamsCommaSeparated = keyParamsCommaSeparated.concat(", ");
                }

            }
            ParameterSpec contextParam = ParameterSpec.builder(ANDROID_CONTEXT, "context")
                    .addAnnotation(NotNull.class)
                    .build();

            MethodSpec getMethod = MethodSpec.methodBuilder(getterName)
                    .addModifiers(PUBLIC,FINAL)
                    .addAnnotation(Override.class)
                    .addAnnotation(NotNull.class)
                    .addParameter(contextParam)
                    .addParameters(keyParameters)
                    .returns(field.getType())
                    .addStatement("$T key = $T.format(STORAGE_KEY,$L)", STRING, STRING, keyParamsCommaSeparated)
                    .addStatement("$T data = this.load($L,key)", storageClassName, ANDROID_CONTEXT)
                    .addStatement("return data.$L", field.getName())
                    .build();

            MethodSpec setMethod = MethodSpec.methodBuilder(getterName)
                    .addModifiers(PUBLIC,FINAL)
                    .addAnnotation(Override.class)
                    .addAnnotation(NotNull.class)
                    .addParameter(field.getType(),"newValue")
                    .addParameter(contextParam)
                    .addParameters(keyParameters)
                    .returns(void.class)
                    .addStatement("$T key = $T.format(STORAGE_KEY,$L)", STRING, STRING, keyParamsCommaSeparated)
                    .addStatement("$T data = this.load($L,key)", storageClassName, ANDROID_CONTEXT)
                    .addStatement("data.$L = $L", field.getName(),"newValue")
                    .addStatement("save($L,$L,$L)","context","data","key")
                    .build();

        });

        TypeSpec.Builder rootClass = TypeSpec
                .classBuilder(instrumentedClassName)
                .addModifiers(PUBLIC, FINAL)
                .superclass(superClass)
                .addField(fieldSpec)
                .addMethod(getStorageClassMethod)
                .addMethod(getDefaultMethod);

        try {
            JavaFile.builder(storagePackage, rootClass.build()).build().writeTo(filer);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }
}
