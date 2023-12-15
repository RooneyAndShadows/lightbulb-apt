package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.LightbulbStorageDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.reader.base.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        generateStorageImplementations(storageDescriptions);
    }

    private void generateStorageImplementations(List<LightbulbStorageDescription> storageDescriptions) {
        storageDescriptions.forEach(storageDescription -> {
            ClassName instrumentedClassName = storageDescription.getInstrumentedClassName();
            ParameterizedTypeName superClass = ParameterizedTypeName.get(BASE_STORAGE, storageDescription.getClassName());
            List<FieldSpec> fields = new ArrayList<>();
            List<MethodSpec> methods = new ArrayList<>();

            generateStorageKeyField(storageDescription, fields);
            generateConstructorMethod(storageDescription, methods);
            generateGetStorageClassMethod(storageDescription, methods);
            generateGetDefaultMethod(storageDescription, methods);
            generateStorageFieldAccessors(storageDescription, methods);

            TypeSpec.Builder rootClass = TypeSpec
                    .classBuilder(instrumentedClassName)
                    .addModifiers(PUBLIC, FINAL)
                    .superclass(superClass)
                    .addFields(fields)
                    .addMethods(methods);

            try {
                JavaFile.builder(storagePackage, rootClass.build()).build().writeTo(filer);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        });
    }

    private void generateStorageKeyField(LightbulbStorageDescription storageDescription, List<FieldSpec> fields) {
        String storageKeyFieldInitializer = String.format("%s.%s", PackageNames.getRootPackage(), storageDescription.getName());

        for (int index = 0; index < storageDescription.getSubKeys().length; index++) {
            storageKeyFieldInitializer = storageKeyFieldInitializer.concat("$%s");
        }

        FieldSpec fieldSpec = FieldSpec.builder(STRING, "STORAGE_KEY", PRIVATE, FINAL)
                .initializer("$S", storageKeyFieldInitializer)
                .build();

        fields.add(fieldSpec);
    }

    private void generateConstructorMethod(LightbulbStorageDescription storageDescription, List<MethodSpec> methods) {
        ParameterSpec activityParam = ParameterSpec.builder(ANDROID_ACTIVITY, "contextActivity")
                .addAnnotation(NotNull.class)
                .build();

        MethodSpec constructorMethod = MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC)
                .addParameter(activityParam)
                .addStatement("super($L)", activityParam.name)
                .build();

        methods.add(constructorMethod);
    }

    private void generateGetStorageClassMethod(LightbulbStorageDescription storageDescription, List<MethodSpec> methods) {
        ClassName storageClassName = storageDescription.getClassName();
        ParameterizedTypeName storageClassTypeName = ParameterizedTypeName.get(CLASS, storageClassName);

        MethodSpec getStorageClassMethod = MethodSpec.methodBuilder("getStorageClass")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .returns(storageClassTypeName)
                .addStatement("return $T.class", storageClassName)
                .build();

        methods.add(getStorageClassMethod);
    }

    private void generateGetDefaultMethod(LightbulbStorageDescription storageDescription, List<MethodSpec> methods) {
        MethodSpec getDefaultMethod = MethodSpec.methodBuilder("getDefault")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addAnnotation(NotNull.class)
                .returns(storageDescription.getClassName())
                .addStatement("return new $T()", storageDescription.getClassName())
                .build();
        methods.add(getDefaultMethod);
    }

    private void generateStorageFieldAccessors(LightbulbStorageDescription storageDescription, List<MethodSpec> methods) {
        ClassName storageClassName = storageDescription.getClassName();

        storageDescription.getFields().forEach(field -> {
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

            ParameterSpec newValueParam = ParameterSpec.builder(field.getType(), "newValue")
                    .addAnnotation(NotNull.class)
                    .build();

            MethodSpec setMethod = MethodSpec.methodBuilder(field.getSetterName())
                    .addModifiers(PUBLIC, FINAL)
                    .addAnnotation(NotNull.class)
                    .addParameter(newValueParam)
                    .addParameters(keyParameters)
                    .returns(void.class)
                    .addStatement("$T key = $T.format(STORAGE_KEY,$L)", STRING, STRING, keyParamsCommaSeparated)
                    .addStatement("$T data = this.load(key)", storageClassName)
                    .addStatement("data.$L = $L", field.getName(), "newValue")
                    .addStatement("save($L,$L)", "data", "key")
                    .build();

            MethodSpec getMethod = MethodSpec.methodBuilder(field.getGetterName())
                    .addModifiers(PUBLIC, FINAL)
                    .addAnnotation(NotNull.class)
                    .addParameters(keyParameters)
                    .returns(field.getType())
                    .addStatement("$T key = $T.format(STORAGE_KEY,$L)", STRING, STRING, keyParamsCommaSeparated)
                    .addStatement("$T data = this.load(key)", storageClassName)
                    .addStatement("return data.$L", field.getName())
                    .build();

            methods.add(setMethod);
            methods.add(getMethod);
        });
    }
}
