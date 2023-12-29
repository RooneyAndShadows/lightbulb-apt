package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.annotation.metadata.ParcelableMetadata;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.entities.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.ParcelableCodeGenerator;
import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.*;
import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames.getParcelablePackage;
import static javax.lang.model.element.Modifier.*;

@SuppressWarnings({"SameParameterValue", "DuplicatedCode"})
public class ParcelableGenerator extends CodeGenerator {
    private final List<ParcelableMetadata> parcelableDescriptions;

    public ParcelableGenerator(Filer filer, Elements elements, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer, elements, annotationResultsRegistry);
        parcelableDescriptions = annotationResultsRegistry.getParcelableDescriptions();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        parcelableDescriptions.forEach(parcelableMetadata -> {
            ClassName instrumentedClassName = parcelableMetadata.getInstrumentedClassName();
            List<FieldSpec> fields = new ArrayList<>();
            List<MethodSpec> methods = new ArrayList<>();

            generateFields(parcelableMetadata, fields);
            generateCreatorField(parcelableMetadata, fields);
            generateConstructorMethod(parcelableMetadata, methods);
            generateWriteToParcelMethod(parcelableMetadata, methods);
            generateDescribeContentsMethod(methods);

            TypeSpec.Builder parcelableClassBuilder = TypeSpec
                    .classBuilder(instrumentedClassName)
                    .addModifiers(PUBLIC, FINAL)
                    .addSuperinterface(ANDROID_PARCELABLE)
                    .addFields(fields)
                    .addMethods(methods);

            writeClassFile(instrumentedClassName.packageName(), parcelableClassBuilder);
        });
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.hasParcelableDescriptions();
    }

    private void generateFields(ParcelableMetadata parcelableMetadata, List<FieldSpec> fields) {
        parcelableMetadata.getTargetFields().forEach(targetField -> {
            Field field = Field.from(targetField);
            TypeName fieldTypeName = field.getTypeInformation().getTypeName();

            FieldSpec fieldSpec = FieldSpec.builder(fieldTypeName, field.getName(), PRIVATE)
                    .build();
            //TODO ADD GETTER SETTER MODIFIERS ETC
            fields.add(fieldSpec);
        });
    }

    private void generateConstructorMethod(ParcelableMetadata parcelableMetadata, List<MethodSpec> methods) {
        MethodSpec.Builder constructorMethodBuilder = MethodSpec.constructorBuilder()
                .addModifiers(PROTECTED)
                .addParameter(ANDROID_PARCEL, "in");

        parcelableMetadata.getTargetFields().forEach(targetField -> {
            Field field = Field.from(targetField);

            CodeBlock readFromParcelBlock = generateReadFromParcelBlock(field, "in");
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

            CodeBlock writeIntoParcelBlock = generateWriteIntoParcelBlock(field, "dest");
            writeToParcelMethodBuilder.addCode(writeIntoParcelBlock);
        });

        methods.add(writeToParcelMethodBuilder.build());
    }

    private void generateCreatorField(ParcelableMetadata parcelableMetadata, List<FieldSpec> fields) {
        ClassName instrumentedClassName = parcelableMetadata.getInstrumentedClassName();
        ParameterizedTypeName typeName = ParameterizedTypeName.get(ANDROID_PARCELABLE_CREATOR, instrumentedClassName);

        TypeSpec.Builder creatorBuilder = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(typeName);

        MethodSpec createFromParcelMethod = MethodSpec.methodBuilder("createFromParcel")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ANDROID_PARCEL, "in")
                .returns(instrumentedClassName)
                .addStatement("return new $T(in)", instrumentedClassName)
                .build();

        ArrayTypeName returnType = ArrayTypeName.of(instrumentedClassName);

        MethodSpec newArrayMethod = MethodSpec.methodBuilder("newArray")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.INT, "size")
                .returns(returnType)
                .addStatement("return new $T[size]", instrumentedClassName)
                .build();

        creatorBuilder.addMethod(createFromParcelMethod);
        creatorBuilder.addMethod(newArrayMethod);

        TypeSpec creator = creatorBuilder.build();

        FieldSpec fieldSpec = FieldSpec.builder(typeName, "CREATOR", PUBLIC, STATIC, FINAL)
                .initializer("$L", creator)
                .build();

        fields.add(fieldSpec);
    }

    private CodeBlock generateWriteIntoParcelBlock(Field field, String bundleVariableName) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        ParcelableCodeGenerator.generateWriteStatement(codeBlock, field, bundleVariableName);

        return codeBlock.build();
    }

    private CodeBlock generateReadFromParcelBlock(Field field, String parcelVariableName) {
        String fieldName = field.getName();
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        ParcelableCodeGenerator.generateReadStatement(codeBlock, field, parcelVariableName);
        generateFieldSetValueStatement(codeBlock, field, fieldName);

        return codeBlock.build();
    }
}
