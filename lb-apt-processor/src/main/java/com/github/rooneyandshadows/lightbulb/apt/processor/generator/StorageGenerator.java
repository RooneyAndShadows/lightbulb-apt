package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.StorageMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.*;
import static javax.lang.model.element.Modifier.*;

public class StorageGenerator extends CodeGenerator {
    private final String storageKeyFieldName = "STORAGE_KEY";
    private final List<StorageMetadata> storageMetadataList;

    public StorageGenerator(Filer filer, Elements elements, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, elements, annotationResultsRegistry);
        storageMetadataList = annotationResultsRegistry.getStorageDescriptions();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        storageMetadataList.forEach(storageDescription -> {
            ClassName instrumentedClassName = storageDescription.getInstrumentedClassName();
            ParameterizedTypeName superClass = ParameterizedTypeName.get(BASE_STORAGE, storageDescription.getClassName());
            List<FieldSpec> fields = new ArrayList<>();
            List<MethodSpec> methods = new ArrayList<>();

            generateStorageKeyField(storageDescription, fields);
            generateConstructorMethod(methods);
            generateGetStorageClassMethod(storageDescription, methods);
            generateGetDefaultMethod(storageDescription, methods);
            generateStorageFieldAccessors(storageDescription, methods);

            TypeSpec.Builder rootClassBuilder = TypeSpec
                    .classBuilder(instrumentedClassName)
                    .addModifiers(PUBLIC, FINAL)
                    .superclass(superClass)
                    .addFields(fields)
                    .addMethods(methods);

            writeClassFile(PackageNames.getStoragePackage(), rootClassBuilder);
        });
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.hasStorageDescriptions();
    }

    private void generateStorageKeyField(StorageMetadata storageMetadata, List<FieldSpec> fields) {
        String storageKeyFieldInitializer = String.format("%s.%s", PackageNames.getRootPackage(), storageMetadata.getName());

        for (int index = 0; index < storageMetadata.getSubKeys().length; index++) {
            storageKeyFieldInitializer = storageKeyFieldInitializer.concat("$%s");
        }

        FieldSpec fieldSpec = FieldSpec.builder(STRING, storageKeyFieldName, PRIVATE, FINAL)
                .initializer("$S", storageKeyFieldInitializer)
                .build();

        fields.add(fieldSpec);
    }

    private void generateConstructorMethod(List<MethodSpec> methods) {
        ParameterSpec activityParam = ParameterSpec.builder(ANDROID_CONTEXT, "context")
                .addAnnotation(NotNull.class)
                .build();

        MethodSpec constructorMethod = MethodSpec.constructorBuilder()
                .addModifiers(PUBLIC)
                .addParameter(activityParam)
                .addStatement("super($L)", activityParam.name)
                .build();

        methods.add(constructorMethod);
    }

    private void generateGetStorageClassMethod(StorageMetadata storageMetadata, List<MethodSpec> methods) {
        ClassName storageClassName = storageMetadata.getClassName();
        ParameterizedTypeName storageClassTypeName = ParameterizedTypeName.get(CLASS, storageClassName);

        MethodSpec getStorageClassMethod = MethodSpec.methodBuilder("getStorageClass")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .returns(storageClassTypeName)
                .addStatement("return $T.class", storageClassName)
                .build();

        methods.add(getStorageClassMethod);
    }

    private void generateGetDefaultMethod(StorageMetadata storageMetadata, List<MethodSpec> methods) {
        MethodSpec getDefaultMethod = MethodSpec.methodBuilder("getDefault")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addAnnotation(NotNull.class)
                .returns(storageMetadata.getClassName())
                .addStatement("return new $T()", storageMetadata.getClassName())
                .build();
        methods.add(getDefaultMethod);
    }

    private void generateStorageFieldAccessors(StorageMetadata storageMetadata, List<MethodSpec> methods) {
        ClassName storageClassName = storageMetadata.getClassName();

        storageMetadata.getTargetFields().forEach(targetField -> {
            Field field = Field.from(targetField);
            TypeName fieldTypeName = field.getTypeInformation().getTypeName();
            String[] subKeys = storageMetadata.getSubKeys();
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

            ParameterSpec newValueParam = ParameterSpec.builder(fieldTypeName, "newValue")
                    .addAnnotation(NotNull.class)
                    .build();


            CodeBlock.Builder loadDataCodeBlock = CodeBlock.builder();

            if (subKeys.length > 0) {
                loadDataCodeBlock.addStatement("$T key = $T.format($L,$L)", STRING, STRING, storageKeyFieldName, keyParamsCommaSeparated);
            } else {
                loadDataCodeBlock.addStatement("$T key = $L", STRING, storageKeyFieldName);
            }

            loadDataCodeBlock.addStatement("$T data = this.load(key)", storageClassName);


            MethodSpec setMethod = MethodSpec.methodBuilder(field.getSetterName())
                    .addModifiers(PUBLIC, FINAL)
                    .addAnnotation(NotNull.class)
                    .addParameter(newValueParam)
                    .addParameters(keyParameters)
                    .returns(void.class)
                    .addCode(loadDataCodeBlock.build())
                    .addStatement("data.$L = $L", field.getName(), "newValue")
                    .addStatement("save($L,$L)", "data", "key")
                    .build();

            MethodSpec getMethod = MethodSpec.methodBuilder(field.getGetterName())
                    .addModifiers(PUBLIC, FINAL)
                    .addAnnotation(NotNull.class)
                    .addParameters(keyParameters)
                    .returns(fieldTypeName)
                    .addCode(loadDataCodeBlock.build())
                    .addStatement("return data.$L", field.getName())
                    .build();

            methods.add(setMethod);
            methods.add(getMethod);
        });
    }
}
