package com.github.rooneyandshadows.lightbulb.apt.processor.generator;

import com.github.rooneyandshadows.lightbulb.apt.processor.data.AnnotationResultsRegistry;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.LightbulbParcelableDescription;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.Field;
import com.github.rooneyandshadows.lightbulb.apt.processor.data.description.common.TypeInformation;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.CodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.generator.base.ParcelableCodeGenerator;
import com.github.rooneyandshadows.lightbulb.apt.processor.utils.PackageNames;
import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.rooneyandshadows.lightbulb.apt.processor.utils.ClassNames.*;
import static javax.lang.model.element.Modifier.*;

@SuppressWarnings("SameParameterValue")
public class ParcelableGenerator extends CodeGenerator {
    private final String parcelablePackage;
    private final List<LightbulbParcelableDescription> parcelableDescriptions;

    public ParcelableGenerator(Filer filer, Elements elements, AnnotationResultsRegistry annotationResultsRegistry) {
        super(filer,elements, annotationResultsRegistry);
        parcelablePackage = PackageNames.getParcelablePackage();
        parcelableDescriptions = annotationResultsRegistry.getParcelableDescriptions();
    }

    @Override
    protected void generateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        generateParcelableImplementations();
    }

    @Override
    protected boolean willGenerateCode(AnnotationResultsRegistry annotationResultsRegistry) {
        return annotationResultsRegistry.hasParcelableDescriptions();
    }

    private void generateParcelableImplementations() {
        parcelableDescriptions.forEach(parcelableDescription -> {
            ClassName instrumentedClassName = parcelableDescription.getInstrumentedClassName();
            List<FieldSpec> fields = new ArrayList<>();
            List<MethodSpec> methods = new ArrayList<>();

            generateFields(parcelableDescription, fields);
            generateCreatorField(parcelableDescription, fields);
            generateConstructorMethod(parcelableDescription, methods);
            generateWriteToParcelMethod(parcelableDescription, methods);
            generateDescribeContentsMethod(methods);


            TypeSpec.Builder parcelableClass = TypeSpec
                    .classBuilder(instrumentedClassName)
                    .addModifiers(PUBLIC, FINAL)
                    .addSuperinterface(ANDROID_PARCELABLE)
                    .addFields(fields)
                    .addMethods(methods);


            try {
                JavaFile.builder(parcelablePackage, parcelableClass.build()).build().writeTo(filer);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        });
    }

    private void generateConstructorMethod(LightbulbParcelableDescription parcelableDescription, List<MethodSpec> methods) {
        MethodSpec.Builder constructorMethodBuilder = MethodSpec.constructorBuilder()
                .addModifiers(PROTECTED)
                .addParameter(ANDROID_PARCEL, "in");

        parcelableDescription.getFields().forEach(field -> {
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

    private void generateWriteToParcelMethod(LightbulbParcelableDescription parcelableDescription, List<MethodSpec> methods) {
        ParameterSpec parcelParam = ParameterSpec.builder(ANDROID_PARCEL, "dest")
                .addAnnotation(NotNull.class)
                .build();

        MethodSpec.Builder writeToParcelMethodBuilder = MethodSpec.methodBuilder("writeToParcel")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(parcelParam)
                .addParameter(TypeName.INT, "flags")
                .returns(void.class);

        parcelableDescription.getFields().forEach(field -> {
            CodeBlock writeIntoParcelBlock = generateWriteIntoParcelBlock(field, "dest");
            writeToParcelMethodBuilder.addCode(writeIntoParcelBlock);
        });

        methods.add(writeToParcelMethodBuilder.build());
    }

    private void generateFields(LightbulbParcelableDescription parcelableDescription, List<FieldSpec> fields) {
        parcelableDescription.getFields().forEach(field -> {
            TypeName fieldTypeName = field.getTypeInformation().getTypeName();

            FieldSpec fieldSpec = FieldSpec.builder(fieldTypeName, field.getName(), PRIVATE)
                    .build();

            fields.add(fieldSpec);
        });
    }

    private void generateCreatorField(LightbulbParcelableDescription parcelableDescription, List<FieldSpec> fields) {
        ParameterizedTypeName typeName = ParameterizedTypeName.get(ANDROID_PARCELABLE_CREATOR, parcelableDescription.getInstrumentedClassName());

        TypeSpec.Builder creatorBuilder = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(typeName);

        MethodSpec createFromParcelMethod = MethodSpec.methodBuilder("createFromParcel")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(ANDROID_PARCEL, "in")
                .returns(parcelableDescription.getInstrumentedClassName())
                .addStatement("return new $T(in)", parcelableDescription.getInstrumentedClassName())
                .build();

        ArrayTypeName returnType = ArrayTypeName.of(parcelableDescription.getInstrumentedClassName());

        MethodSpec newArrayMethod = MethodSpec.methodBuilder("newArray")
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.INT, "size")
                .returns(returnType)
                .addStatement("return new $T[size]", parcelableDescription.getInstrumentedClassName())
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
        generateFieldSetValueStatement(codeBlock, field, fieldName, false);

        return codeBlock.build();
    }
}
